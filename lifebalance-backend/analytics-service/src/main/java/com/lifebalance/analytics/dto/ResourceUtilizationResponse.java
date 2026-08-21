package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.EvaluationStatus;
import java.math.BigDecimal;

public record ResourceUtilizationResponse(
        String resourceType,
        String unit,
        BigDecimal plannedAmount,
        BigDecimal actualAmount,
        BigDecimal varianceAmount,
        BigDecimal efficiencyPercent,
        BigDecimal planningAccuracyPercent,
        long measuredTaskCount,
        long missingPlanCount,
        long missingActualCount,
        EvaluationStatus aggregateStatus
) {
}
