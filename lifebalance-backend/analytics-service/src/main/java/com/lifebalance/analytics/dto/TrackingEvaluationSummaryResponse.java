package com.lifebalance.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TrackingEvaluationSummaryResponse(
        UUID ownerId,
        LocalDate periodStart,
        LocalDate periodEnd,
        String currencyCode,
        long evaluatedTaskCount,
        long comparableEvaluationCount,
        long actualRecordCount,
        long onTrackCount,
        long underPlannedCount,
        long overPlannedCount,
        long noPlanCount,
        Integer plannedMinutes,
        Integer actualMinutes,
        Integer minuteVariance,
        BigDecimal plannedCost,
        BigDecimal actualCost,
        BigDecimal costVariance,
        BigDecimal timeEfficiencyPercent,
        BigDecimal costEfficiencyPercent,
        BigDecimal overallEfficiencyPercent,
        BigDecimal planningAccuracyPercent,
        BigDecimal productivityScore,
        BigDecimal dataCompletenessPercent
) {
}
