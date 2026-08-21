package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AiRecommendationResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        AiRecommendationType recommendationType,
        AiRecommendationStatus status,
        AiPriority priority,
        String title,
        String description,
        String sourceType,
        UUID sourceId,
        String targetType,
        UUID targetId,
        BigDecimal confidenceScore,
        String signalSummary,
        OffsetDateTime generatedAt,
        OffsetDateTime decidedAt,
        String decisionReason,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
