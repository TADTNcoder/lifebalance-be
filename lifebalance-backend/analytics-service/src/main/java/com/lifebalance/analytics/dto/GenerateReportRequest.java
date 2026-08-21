package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record GenerateReportRequest(
        @NotNull ReportType reportType,
        @NotNull ReportDimension dimension,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        @Size(min = 3, max = 3) String currencyCode,
        @Size(max = 1000) String reason
) {
}
