package com.lifebalance.timeline.dto;

import com.lifebalance.timeline.domain.TimelineConflictPolicy;
import com.lifebalance.timeline.domain.TimelinePlacementSource;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimelinePlacementResponse(
        UUID id,
        UUID ownerId,
        UUID taskId,
        String taskTitle,
        TimelineTaskStatus taskStatus,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String timezone,
        TimelinePlacementSource source,
        TimelinePlacementStatus status,
        TimelineConflictPolicy conflictPolicy,
        boolean conflicted,
        boolean conflictConfirmed,
        String conflictReason,
        String reason,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
