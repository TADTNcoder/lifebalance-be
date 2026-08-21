package com.lifebalance.timeline.service.impl;

import com.lifebalance.timeline.domain.TimelineConflictPolicy;
import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelinePlacementSource;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.dto.CancelTimelinePlacementRequest;
import com.lifebalance.timeline.dto.RescheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.ScheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.TimelineAvailabilityResponse;
import com.lifebalance.timeline.dto.TimelineConflictResponse;
import com.lifebalance.timeline.dto.TimelinePlacementResponse;
import com.lifebalance.timeline.error.TimelineExceptions;
import com.lifebalance.timeline.repository.TimelinePlacementRepository;
import com.lifebalance.timeline.repository.TimelineTaskRepository;
import com.lifebalance.timeline.service.TimelinePlacementService;
import com.lifebalance.timeline.validation.TimelinePolicyProperties;
import com.lifebalance.timeline.validation.TimelineScheduleValidator;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TimelinePlacementServiceImpl implements TimelinePlacementService {

    private final TimelineTaskRepository taskRepository;
    private final TimelinePlacementRepository placementRepository;
    private final TimelineScheduleValidator validator;
    private final TimelinePolicyProperties policyProperties;
    private final TimelineHistoryRecorder historyRecorder;
    private final TimelineMapper mapper;

    TimelinePlacementServiceImpl(
            TimelineTaskRepository taskRepository,
            TimelinePlacementRepository placementRepository,
            TimelineScheduleValidator validator,
            TimelinePolicyProperties policyProperties,
            TimelineHistoryRecorder historyRecorder,
            TimelineMapper mapper
    ) {
        this.taskRepository = taskRepository;
        this.placementRepository = placementRepository;
        this.validator = validator;
        this.policyProperties = policyProperties;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TimelinePlacementResponse schedule(UUID ownerId, ScheduleTimelinePlacementRequest request) {
        TimelineTask task = findTaskForUpdate(ownerId, request.taskId());
        validator.validateTaskEligibility(task);
        validator.validateTaskWindow(task, request.startAt(), request.endAt());

        ConflictDecision decision = resolveConflictDecision(
                ownerId,
                null,
                request.startAt(),
                request.endAt(),
                request.conflictConfirmed(),
                request.conflictReason()
        );

        task.markScheduled(request.startAt(), request.endAt(), ownerId);
        taskRepository.save(task);

        TimelinePlacement placement = TimelinePlacement.schedule(
                ownerId,
                ownerId,
                task,
                request.startAt(),
                request.endAt(),
                request.timezone(),
                TimelinePlacementSource.MANUAL,
                decision.policy(),
                decision.conflicted(),
                decision.confirmed(),
                request.conflictReason(),
                request.reason()
        );
        placement = placementRepository.save(placement);
        historyRecorder.record(
                ownerId,
                ownerId,
                TimelineHistoryActionType.TIMELINE_SCHEDULED,
                placement,
                task,
                null,
                mapper.placementSnapshot(placement),
                request.reason()
        );
        recordConflictConfirmationIfNeeded(ownerId, placement, task, decision, request.conflictReason());
        return mapper.toPlacementResponse(placement);
    }

    @Override
    @Transactional
    public TimelinePlacementResponse reschedule(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request
    ) {
        return changePlacement(
                ownerId,
                placementId,
                request,
                TimelinePlacementSource.MANUAL,
                TimelineHistoryActionType.TIMELINE_RESCHEDULED
        );
    }

    @Override
    @Transactional
    public TimelinePlacementResponse move(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request
    ) {
        return changePlacement(
                ownerId,
                placementId,
                request,
                TimelinePlacementSource.DRAG_DROP,
                TimelineHistoryActionType.TIMELINE_MOVED
        );
    }

    @Override
    @Transactional
    public TimelinePlacementResponse cancel(UUID ownerId, UUID placementId, CancelTimelinePlacementRequest request) {
        TimelinePlacement placement = findPlacementForUpdate(ownerId, placementId);
        TimelineTask task = findTaskForUpdate(ownerId, placement.getTask().getId());
        String oldSnapshot = mapper.placementSnapshot(placement);
        String reason = request == null ? null : request.reason();

        placement.cancel(ownerId, reason);
        task.clearScheduleIfOnlyScheduled(ownerId);
        taskRepository.save(task);
        placement = placementRepository.save(placement);
        historyRecorder.record(
                ownerId,
                ownerId,
                TimelineHistoryActionType.TIMELINE_CANCELLED,
                placement,
                task,
                oldSnapshot,
                mapper.placementSnapshot(placement),
                reason
        );
        return mapper.toPlacementResponse(placement);
    }

    @Override
    @Transactional
    public TimelinePlacementResponse archive(UUID ownerId, UUID placementId, CancelTimelinePlacementRequest request) {
        TimelinePlacement placement = findPlacementForUpdate(ownerId, placementId);
        TimelineTask task = findTaskForUpdate(ownerId, placement.getTask().getId());
        String oldSnapshot = mapper.placementSnapshot(placement);
        String reason = request == null ? null : request.reason();

        placement.archive(ownerId, reason);
        task.clearScheduleIfOnlyScheduled(ownerId);
        taskRepository.save(task);
        placement = placementRepository.save(placement);
        historyRecorder.record(
                ownerId,
                ownerId,
                TimelineHistoryActionType.TIMELINE_ARCHIVED,
                placement,
                task,
                oldSnapshot,
                mapper.placementSnapshot(placement),
                reason
        );
        return mapper.toPlacementResponse(placement);
    }

    @Override
    @Transactional(readOnly = true)
    public TimelinePlacementResponse getById(UUID ownerId, UUID placementId) {
        return mapper.toPlacementResponse(placementRepository
                .findByIdAndOwnerId(placementId, ownerId)
                .orElseThrow(() -> TimelineExceptions.placementNotFound(placementId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelinePlacementResponse> getTimeline(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            TimelinePlacementStatus status,
            Pageable pageable
    ) {
        validator.validateWindow(from, to);
        TimelinePlacementStatus normalizedStatus = status == null ? TimelinePlacementStatus.ACTIVE : status;
        return placementRepository
                .findTimeline(ownerId, normalizedStatus, from, to, pageable)
                .map(mapper::toPlacementResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TimelineAvailabilityResponse getAvailability(UUID ownerId, OffsetDateTime from, OffsetDateTime to) {
        validator.validateWindow(from, to);
        List<TimelineConflictResponse> conflicts = placementRepository
                .findConflicts(ownerId, TimelinePlacementStatus.ACTIVE, null, from, to)
                .stream()
                .map(mapper::toConflictResponse)
                .toList();
        return new TimelineAvailabilityResponse(ownerId, from, to, conflicts.isEmpty(), conflicts);
    }

    private TimelinePlacementResponse changePlacement(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request,
            TimelinePlacementSource source,
            TimelineHistoryActionType actionType
    ) {
        TimelinePlacement placement = findPlacementForUpdate(ownerId, placementId);
        TimelineTask task = findTaskForUpdate(ownerId, placement.getTask().getId());
        validator.validateTaskEligibility(task);
        validator.validateTaskWindow(task, request.startAt(), request.endAt());

        ConflictDecision decision = resolveConflictDecision(
                ownerId,
                placementId,
                request.startAt(),
                request.endAt(),
                request.conflictConfirmed(),
                request.conflictReason()
        );

        String oldSnapshot = mapper.placementSnapshot(placement);
        placement.reschedule(
                ownerId,
                request.startAt(),
                request.endAt(),
                request.timezone(),
                source,
                decision.policy(),
                decision.conflicted(),
                decision.confirmed(),
                request.conflictReason(),
                request.reason()
        );
        task.markScheduled(request.startAt(), request.endAt(), ownerId);
        taskRepository.save(task);
        placement = placementRepository.save(placement);
        historyRecorder.record(
                ownerId,
                ownerId,
                actionType,
                placement,
                task,
                oldSnapshot,
                mapper.placementSnapshot(placement),
                request.reason()
        );
        recordConflictConfirmationIfNeeded(ownerId, placement, task, decision, request.conflictReason());
        return mapper.toPlacementResponse(placement);
    }

    private TimelineTask findTaskForUpdate(UUID ownerId, UUID taskId) {
        return taskRepository
                .findByIdAndOwnerIdForUpdate(taskId, ownerId)
                .orElseThrow(() -> TimelineExceptions.taskNotFound(taskId));
    }

    private TimelinePlacement findPlacementForUpdate(UUID ownerId, UUID placementId) {
        return placementRepository
                .findByIdAndOwnerIdForUpdate(placementId, ownerId)
                .orElseThrow(() -> TimelineExceptions.placementNotFound(placementId));
    }

    private ConflictDecision resolveConflictDecision(
            UUID ownerId,
            UUID excludedPlacementId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            Boolean conflictConfirmed,
            String conflictReason
    ) {
        List<TimelinePlacement> conflicts = placementRepository.findConflicts(
                ownerId,
                TimelinePlacementStatus.ACTIVE,
                excludedPlacementId,
                startAt,
                endAt
        );
        if (conflicts.isEmpty()) {
            return new ConflictDecision(policyProperties.getConflictPolicy(), false, false, 0);
        }

        TimelineConflictPolicy policy = policyProperties.getConflictPolicy();
        if (policy == TimelineConflictPolicy.REJECT) {
            throw TimelineExceptions.conflict(ownerId, conflicts.size());
        }
        if (policy == TimelineConflictPolicy.ALLOW_WITH_CONFIRMATION && !Boolean.TRUE.equals(conflictConfirmed)) {
            throw TimelineExceptions.conflictConfirmationRequired(ownerId, conflicts.size());
        }
        if (policy == TimelineConflictPolicy.ALLOW_WITH_CONFIRMATION
                && (conflictReason == null || conflictReason.isBlank())) {
            throw TimelineExceptions.conflictConfirmationRequired(ownerId, conflicts.size());
        }

        return new ConflictDecision(
                policy,
                true,
                policy == TimelineConflictPolicy.ALLOW_WITH_CONFIRMATION,
                conflicts.size()
        );
    }

    private void recordConflictConfirmationIfNeeded(
            UUID ownerId,
            TimelinePlacement placement,
            TimelineTask task,
            ConflictDecision decision,
            String conflictReason
    ) {
        if (!decision.confirmed()) {
            return;
        }
        historyRecorder.record(
                ownerId,
                ownerId,
                TimelineHistoryActionType.TIMELINE_CONFLICT_CONFIRMED,
                placement,
                task,
                null,
                "placementId=" + placement.getId()
                        + ";conflictCount=" + decision.conflictCount()
                        + ";policy=" + decision.policy(),
                conflictReason
        );
    }

    private record ConflictDecision(
            TimelineConflictPolicy policy,
            boolean conflicted,
            boolean confirmed,
            int conflictCount
    ) {
    }
}
