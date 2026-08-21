package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiConversationStatus;
import com.lifebalance.ai.domain.AiIntent;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AiConversationResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        String title,
        AiIntent intent,
        String contextType,
        UUID contextId,
        AiConversationStatus status,
        OffsetDateTime archivedAt,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
