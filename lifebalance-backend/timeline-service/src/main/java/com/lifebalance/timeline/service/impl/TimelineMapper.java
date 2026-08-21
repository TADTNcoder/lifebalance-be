package com.lifebalance.timeline.service.impl;

import com.lifebalance.timeline.domain.TimelineHistory;
import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.dto.TimelineConflictResponse;
import com.lifebalance.timeline.dto.TimelineHistoryResponse;
import com.lifebalance.timeline.dto.TimelinePlacementResponse;
import com.lifebalance.timeline.dto.TimelineTaskResponse;
import org.springframework.stereotype.Component;

@Component
class TimelineMapper {

    TimelineTaskResponse toTaskResponse(TimelineTask task) {
        return new TimelineTaskResponse(
                task.getId(),
                task.getOwnerId(),
                task.getTitle(),
                task.getTaskStatus(),
                task.isHasTimeCapital(),
                task.getEstimatedMinutes(),
                task.getDeadline(),
                task.getCapitalCycleId(),
                task.getCycleStartAt(),
                task.getCycleEndAt(),
                task.getScheduledStartAt(),
                task.getScheduledEndAt(),
                task.isTimelineEligible(),
                task.getCreatedBy(),
                task.getUpdatedBy(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    TimelinePlacementResponse toPlacementResponse(TimelinePlacement placement) {
        TimelineTask task = placement.getTask();
        return new TimelinePlacementResponse(
                placement.getId(),
                placement.getOwnerId(),
                task.getId(),
                task.getTitle(),
                task.getTaskStatus(),
                placement.getStartAt(),
                placement.getEndAt(),
                placement.getTimezone(),
                placement.getSource(),
                placement.getStatus(),
                placement.getConflictPolicy(),
                placement.isConflicted(),
                placement.isConflictConfirmed(),
                placement.getConflictReason(),
                placement.getReason(),
                placement.getCreatedBy(),
                placement.getUpdatedBy(),
                placement.getCreatedAt(),
                placement.getUpdatedAt()
        );
    }

    TimelineConflictResponse toConflictResponse(TimelinePlacement placement) {
        TimelineTask task = placement.getTask();
        return new TimelineConflictResponse(
                placement.getId(),
                task.getId(),
                task.getTitle(),
                placement.getStartAt(),
                placement.getEndAt()
        );
    }

    TimelineHistoryResponse toHistoryResponse(TimelineHistory history) {
        TimelinePlacement placement = history.getPlacement();
        TimelineTask task = history.getTask();
        return new TimelineHistoryResponse(
                history.getId(),
                history.getOwnerId(),
                history.getActorId(),
                history.getActionType(),
                placement == null ? null : placement.getId(),
                task == null ? null : task.getId(),
                history.getOldValue(),
                history.getNewValue(),
                history.getReason(),
                history.getOccurredAt()
        );
    }

    String placementSnapshot(TimelinePlacement placement) {
        return "status=" + placement.getStatus()
                + ";taskId=" + placement.getTask().getId()
                + ";startAt=" + placement.getStartAt()
                + ";endAt=" + placement.getEndAt()
                + ";timezone=" + placement.getTimezone()
                + ";source=" + placement.getSource()
                + ";conflicted=" + placement.isConflicted()
                + ";conflictConfirmed=" + placement.isConflictConfirmed();
    }

    String taskSnapshot(TimelineTask task) {
        return "taskId=" + task.getId()
                + ";status=" + task.getTaskStatus()
                + ";title=" + task.getTitle()
                + ";hasTimeCapital=" + task.isHasTimeCapital()
                + ";estimatedMinutes=" + task.getEstimatedMinutes()
                + ";deadline=" + task.getDeadline()
                + ";cycleStartAt=" + task.getCycleStartAt()
                + ";cycleEndAt=" + task.getCycleEndAt()
                + ";scheduledStartAt=" + task.getScheduledStartAt()
                + ";scheduledEndAt=" + task.getScheduledEndAt();
    }
}
