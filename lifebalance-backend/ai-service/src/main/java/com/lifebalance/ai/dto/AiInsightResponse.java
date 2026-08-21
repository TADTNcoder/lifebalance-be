package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightStatus;
import com.lifebalance.ai.domain.AiInsightType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AiInsightResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        AiInsightType insightType,
        AiInsightSeverity severity,
        AiInsightStatus status,
        String title,
        String summary,
        LocalDate periodStart,
        LocalDate periodEnd,
        String referenceType,
        UUID referenceId,
        BigDecimal confidenceScore,
        String signalSummary,
        OffsetDateTime generatedAt,
        OffsetDateTime archivedAt,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
