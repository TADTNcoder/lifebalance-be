package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiHistoryActionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AiHistoryResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        AiHistoryActionType actionType,
        UUID conversationId,
        UUID messageId,
        UUID recommendationId,
        UUID insightId,
        String oldValue,
        String newValue,
        String reason,
        OffsetDateTime occurredAt
) {
}
