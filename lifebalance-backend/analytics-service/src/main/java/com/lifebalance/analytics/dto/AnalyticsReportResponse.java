package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportStatus;
import com.lifebalance.analytics.domain.ReportType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AnalyticsReportResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        ReportType reportType,
        ReportStatus status,
        ReportDimension dimension,
        LocalDate periodStart,
        LocalDate periodEnd,
        Integer taskCount,
        Integer actualRecordCount,
        Integer totalActualMinutes,
        BigDecimal totalActualCost,
        String currencyCode,
        BigDecimal averageEfficiencyPercent,
        String varianceSummary,
        OffsetDateTime generatedAt,
        String reason,
        OffsetDateTime archivedAt,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
