package com.lifebalance.analytics.dto;

import java.math.BigDecimal;

public record EvaluationDeltaResponse(
        long evaluatedTaskCountDelta,
        long actualRecordCountDelta,
        Integer plannedMinutesDelta,
        Integer actualMinutesDelta,
        Integer minuteVarianceDelta,
        BigDecimal plannedCostDelta,
        BigDecimal actualCostDelta,
        BigDecimal costVarianceDelta,
        BigDecimal averageEfficiencyPercentDelta,
        BigDecimal planningAccuracyPercentDelta,
        BigDecimal productivityScoreDelta
) {
}
