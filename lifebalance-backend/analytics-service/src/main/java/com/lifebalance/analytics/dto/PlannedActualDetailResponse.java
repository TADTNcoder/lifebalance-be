package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.EvaluationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlannedActualDetailResponse(
        UUID evaluationId,
        UUID taskId,
        UUID capitalCycleId,
        LocalDate periodStart,
        LocalDate periodEnd,
        Integer plannedMinutes,
        Integer actualMinutes,
        Integer minuteVariance,
        BigDecimal plannedCost,
        BigDecimal actualCost,
        BigDecimal costVariance,
        String currencyCode,
        BigDecimal efficiencyPercent,
        BigDecimal planningAccuracyPercent,
        EvaluationStatus status,
        OffsetDateTime generatedAt
) {
}
