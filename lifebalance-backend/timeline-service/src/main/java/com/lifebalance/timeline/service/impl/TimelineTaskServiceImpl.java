package com.lifebalance.timeline.service.impl;

import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import com.lifebalance.timeline.dto.TimelineTaskResponse;
import com.lifebalance.timeline.dto.UpsertTimelineTaskRequest;
import com.lifebalance.timeline.error.TimelineExceptions;
import com.lifebalance.timeline.repository.TimelinePlacementRepository;
import com.lifebalance.timeline.repository.TimelineTaskRepository;
import com.lifebalance.timeline.service.TimelineTaskService;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TimelineTaskServiceImpl implements TimelineTaskService {

    private static final Set<TimelineTaskStatus> ELIGIBLE_STATUSES = EnumSet.of(
            TimelineTaskStatus.DRAFT,
            TimelineTaskStatus.PLANNED,
            TimelineTaskStatus.SCHEDULED
    );

    private final TimelineTaskRepository taskRepository;
    private final TimelinePlacementRepository placementRepository;
    private final TimelineHistoryRecorder historyRecorder;
    private final TimelineMapper mapper;

    TimelineTaskServiceImpl(
            TimelineTaskRepository taskRepository,
            TimelinePlacementRepository placementRepository,
            TimelineHistoryRecorder historyRecorder,
            TimelineMapper mapper
    ) {
        this.taskRepository = taskRepository;
        this.placementRepository = placementRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TimelineTaskResponse upsertTask(UUID ownerId, UpsertTimelineTaskRequest request) {
        Optional<TimelineTask> existingTask = taskRepository.findByIdAndOwnerIdForUpdate(request.taskId(), ownerId);
        if (existingTask.isPresent()) {
            TimelineTask task = existingTask.get();
            String oldSnapshot = mapper.taskSnapshot(task);
            task.applySnapshot(
                    ownerId,
                    request.title(),
                    request.taskStatus(),
                    Boolean.TRUE.equals(request.hasTimeCapital()),
                    request.estimatedMinutes(),
                    request.deadline(),
                    request.capitalCycleId(),
                    request.cycleStartAt(),
                    request.cycleEndAt(),
                    request.scheduledStartAt(),
                    request.scheduledEndAt()
            );
            task = taskRepository.save(task);
            historyRecorder.record(
                    ownerId,
                    ownerId,
                    TimelineHistoryActionType.TASK_SNAPSHOT_UPDATED,
                    null,
                    task,
                    oldSnapshot,
                    mapper.taskSnapshot(task),
                    "Task snapshot synchronized"
            );
            closeActivePlacementsIfTaskIsNoLongerVisible(ownerId, task);
            return mapper.toTaskResponse(task);
        }

        if (taskRepository.findById(request.taskId()).isPresent()) {
            throw TimelineExceptions.taskNotFound(request.taskId());
        }

        TimelineTask task = TimelineTask.register(
                ownerId,
                ownerId,
                request.taskId(),
                request.title(),
                request.taskStatus(),
                Boolean.TRUE.equals(request.hasTimeCapital()),
                request.estimatedMinutes(),
                request.deadline(),
                request.capitalCycleId(),
                request.cycleStartAt(),
                request.cycleEndAt(),
                request.scheduledStartAt(),
                request.scheduledEndAt()
        );
        task = taskRepository.save(task);
        historyRecorder.record(
                ownerId,
                ownerId,
                TimelineHistoryActionType.TASK_SNAPSHOT_REGISTERED,
                null,
                task,
                null,
                mapper.taskSnapshot(task),
                "Task snapshot registered"
        );
        return mapper.toTaskResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TimelineTaskResponse getTask(UUID ownerId, UUID taskId) {
        return mapper.toTaskResponse(taskRepository
                .findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(() -> TimelineExceptions.taskNotFound(taskId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelineTaskResponse> getEligibleTasks(UUID ownerId, Pageable pageable) {
        return taskRepository
                .findEligibleTasks(ownerId, ELIGIBLE_STATUSES, pageable)
                .map(mapper::toTaskResponse);
    }

    private void closeActivePlacementsIfTaskIsNoLongerVisible(UUID ownerId, TimelineTask task) {
        if (task.isVisibleOnMainTimeline()) {
            return;
        }

        List<TimelinePlacement> activePlacements = placementRepository.findByOwnerIdAndTaskIdAndStatus(
                ownerId,
                task.getId(),
                TimelinePlacementStatus.ACTIVE
        );
        for (TimelinePlacement placement : activePlacements) {
            String oldSnapshot = mapper.placementSnapshot(placement);
            if (task.getTaskStatus() == TimelineTaskStatus.ARCHIVED) {
                placement.archive(ownerId, "Task snapshot status changed to ARCHIVED");
                placementRepository.save(placement);
                historyRecorder.record(
                        ownerId,
                        ownerId,
                        TimelineHistoryActionType.TIMELINE_ARCHIVED,
                        placement,
                        task,
                        oldSnapshot,
                        mapper.placementSnapshot(placement),
                        "Task snapshot status changed to ARCHIVED"
                );
            } else {
                placement.cancel(ownerId, "Task snapshot status changed to CANCELLED");
                placementRepository.save(placement);
                historyRecorder.record(
                        ownerId,
                        ownerId,
                        TimelineHistoryActionType.TIMELINE_CANCELLED,
                        placement,
                        task,
                        oldSnapshot,
                        mapper.placementSnapshot(placement),
                        "Task snapshot status changed to CANCELLED"
                );
            }
            task.clearScheduleIfOnlyScheduled(ownerId);
        }
    }
}
