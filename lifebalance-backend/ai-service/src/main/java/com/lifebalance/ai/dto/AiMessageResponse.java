package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.domain.AiMessageRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AiMessageResponse(
        UUID id,
        UUID conversationId,
        UUID ownerId,
        UUID actorId,
        AiMessageRole role,
        AiIntent intent,
        String content,
        String modelName,
        Integer tokenEstimate,
        OffsetDateTime createdAt
) {
}
