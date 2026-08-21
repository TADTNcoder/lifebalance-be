package com.lifebalance.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AnalyticsDashboardResponse(
        UUID ownerId,
        LocalDate periodStart,
        LocalDate periodEnd,
        long actualRecordCount,
        long evaluatedTaskCount,
        int totalActualMinutes,
        BigDecimal totalActualCost,
        String currencyCode,
        BigDecimal averageEfficiencyPercent,
        long overPlannedCount,
        long underPlannedCount,
        long onTrackCount
) {
}
