package com.lifebalance.task.service.impl;

import com.lifebalance.task.dto.request.CancelTimelinePlacementRequest;
import com.lifebalance.task.dto.request.RescheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.request.ScheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.response.TimelinePlacementResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.integration.TaskIntegrationAction;
import com.lifebalance.task.integration.TaskIntegrationPublisher;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.repository.TimelinePlacementRepository;
import com.lifebalance.task.service.TimelinePlacementService;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimelinePlacementServiceImpl implements TimelinePlacementService {

    private final TaskRepository taskRepository;
    private final TimelinePlacementRepository timelinePlacementRepository;
    private final TaskLifecyclePolicy taskLifecyclePolicy;
    private final TaskChangeHistoryService taskChangeHistoryService;
    private final TaskIntegrationPublisher taskIntegrationPublisher;

    @Override
    @Transactional
    public TimelinePlacementResponse schedule(
            UUID ownerId,
            ScheduleTimelinePlacementRequest request) {

        Task task = findTask(ownerId, request.getTaskId());
        taskLifecyclePolicy.validateTimelineEligibility(task);
        taskLifecyclePolicy.validateTimelineWindow(
                request.getStartAt(),
                request.getEndAt());
        ensureNoTimelineConflict(
                ownerId,
                null,
                request.getStartAt(),
                request.getEndAt());

        task.schedule(
                request.getStartAt(),
                request.getEndAt());
        task.setUpdatedBy(ownerId);
        taskRepository.save(task);

        TimelinePlacement placement = TimelinePlacement.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .task(task)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .timezone(request.getTimezone())
                .source("MANUAL")
                .status(TimelinePlacementStatus.ACTIVE)
                .reason(request.getReason())
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();

        placement = timelinePlacementRepository.save(placement);
        taskChangeHistoryService.recordTimelineChange(
                task,
                placement,
                ownerId,
                TaskHistoryActionType.TIMELINE_SCHEDULED,
                null,
                timelineSnapshot(placement),
                request.getReason());
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_SCHEDULED,
                request.getReason());

        return mapToResponse(placement);
    }

    @Override
    @Transactional
    public TimelinePlacementResponse reschedule(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request) {

        return changePlacement(
                ownerId,
                placementId,
                request,
                TaskHistoryActionType.TIMELINE_RESCHEDULED);
    }

    @Override
    @Transactional
    public TimelinePlacementResponse move(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request) {

        return changePlacement(
                ownerId,
                placementId,
                request,
                TaskHistoryActionType.TIMELINE_MOVED);
    }

    @Override
    @Transactional
    public void cancel(
            UUID ownerId,
            UUID placementId,
            CancelTimelinePlacementRequest request) {

        TimelinePlacement placement = findPlacement(ownerId, placementId);
        Task task = placement.getTask();
        if (!placement.isActive()) {
            throw TaskExceptions.timelinePlacementNotFound();
        }

        String oldSnapshot = timelineSnapshot(placement);
        placement.cancel(
                request == null ? null : request.getReason(),
                ownerId);

        if (task.getStatus() == TaskStatus.SCHEDULED) {
            taskLifecyclePolicy.validateTransition(
                    task.getStatus(),
                    TaskStatus.PLANNED);
            task.transitionTo(TaskStatus.PLANNED);
            task.setScheduledWindow(null, null);
            task.setUpdatedBy(ownerId);
            taskRepository.save(task);
        }

        timelinePlacementRepository.save(placement);
        taskChangeHistoryService.recordTimelineChange(
                task,
                placement,
                ownerId,
                TaskHistoryActionType.TIMELINE_CANCELLED,
                oldSnapshot,
                timelineSnapshot(placement),
                request == null ? null : request.getReason());
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_TIMELINE_CANCELLED,
                request == null ? null : request.getReason());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelinePlacementResponse> getTimeline(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable) {

        taskLifecyclePolicy.validateTimelineWindow(
                from,
                to);

        return timelinePlacementRepository
                .findActiveTimeline(
                        ownerId,
                        TimelinePlacementStatus.ACTIVE,
                        from,
                        to,
                        pageable)
                .map(this::mapToResponse);
    }

    private TimelinePlacementResponse changePlacement(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request,
            TaskHistoryActionType actionType) {

        TimelinePlacement placement = findPlacement(ownerId, placementId);
        if (!placement.isActive()) {
            throw TaskExceptions.timelinePlacementNotFound();
        }

        Task task = placement.getTask();
        taskLifecyclePolicy.validateTimelineEligibility(task);
        taskLifecyclePolicy.validateTimelineWindow(
                request.getStartAt(),
                request.getEndAt());
        ensureNoTimelineConflict(
                ownerId,
                placementId,
                request.getStartAt(),
                request.getEndAt());

        String oldSnapshot = timelineSnapshot(placement);
        placement.reschedule(
                request.getStartAt(),
                request.getEndAt(),
                request.getTimezone(),
                request.getReason(),
                ownerId);
        if (actionType == TaskHistoryActionType.TIMELINE_MOVED) {
            placement.setSource("DRAG_DROP");
        }
        task.schedule(
                request.getStartAt(),
                request.getEndAt());
        task.setUpdatedBy(ownerId);

        taskRepository.save(task);
        placement = timelinePlacementRepository.save(placement);
        taskChangeHistoryService.recordTimelineChange(
                task,
                placement,
                ownerId,
                actionType,
                oldSnapshot,
                timelineSnapshot(placement),
                request.getReason());
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                actionType == TaskHistoryActionType.TIMELINE_MOVED
                        ? TaskIntegrationAction.TASK_MOVED
                        : TaskIntegrationAction.TASK_RESCHEDULED,
                request.getReason());

        return mapToResponse(placement);
    }

    private Task findTask(
            UUID ownerId,
            UUID taskId) {

        return taskRepository
                .findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);
    }

    private TimelinePlacement findPlacement(
            UUID ownerId,
            UUID placementId) {

        return timelinePlacementRepository
                .findByIdAndOwnerId(placementId, ownerId)
                .orElseThrow(TaskExceptions::timelinePlacementNotFound);
    }

    private void ensureNoTimelineConflict(
            UUID ownerId,
            UUID excludedPlacementId,
            OffsetDateTime startAt,
            OffsetDateTime endAt) {

        if (timelinePlacementRepository.existsOverlappingPlacement(
                ownerId,
                TimelinePlacementStatus.ACTIVE,
                excludedPlacementId,
                startAt,
                endAt)) {

            throw TaskExceptions.timelineConflict();
        }
    }

    private TimelinePlacementResponse mapToResponse(TimelinePlacement placement) {
        TimelinePlacementResponse response = new TimelinePlacementResponse();
        response.setId(placement.getId());
        response.setOwnerId(placement.getOwnerId());
        response.setUserId(placement.getUserId());
        response.setTaskId(placement.getTask().getId());
        response.setTaskName(placement.getTask().getName());
        response.setStartAt(placement.getStartAt());
        response.setEndAt(placement.getEndAt());
        response.setTimezone(placement.getTimezone());
        response.setSource(placement.getSource());
        response.setStatus(placement.getStatus());
        response.setReason(placement.getReason());
        response.setCreatedBy(placement.getCreatedBy());
        response.setUpdatedBy(placement.getUpdatedBy());
        response.setCreatedAt(placement.getCreatedAt());
        response.setUpdatedAt(placement.getUpdatedAt());
        return response;
    }

    private String timelineSnapshot(TimelinePlacement placement) {
        return "status=" + placement.getStatus()
                + ";taskId=" + placement.getTask().getId()
                + ";startAt=" + placement.getStartAt()
                + ";endAt=" + placement.getEndAt()
                + ";timezone=" + placement.getTimezone()
                + ";source=" + placement.getSource();
    }
}
