package com.lifebalance.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluationTrendPointResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        long evaluatedTaskCount,
        long actualRecordCount,
        Integer plannedMinutes,
        Integer actualMinutes,
        Integer minuteVariance,
        BigDecimal plannedCost,
        BigDecimal actualCost,
        BigDecimal costVariance,
        BigDecimal averageEfficiencyPercent,
        BigDecimal planningAccuracyPercent,
        BigDecimal productivityScore
) {
}
