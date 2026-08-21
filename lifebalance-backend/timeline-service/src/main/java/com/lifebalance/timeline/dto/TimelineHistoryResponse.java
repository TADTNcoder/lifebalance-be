package com.lifebalance.timeline.dto;

import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimelineHistoryResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        TimelineHistoryActionType actionType,
        UUID placementId,
        UUID taskId,
        String oldValue,
        String newValue,
        String reason,
        OffsetDateTime occurredAt
) {
}
