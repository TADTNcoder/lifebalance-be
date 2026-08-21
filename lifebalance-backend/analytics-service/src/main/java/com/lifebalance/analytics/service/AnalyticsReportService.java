package com.lifebalance.analytics.service;

import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportExportFormat;
import com.lifebalance.analytics.domain.ReportStatus;
import com.lifebalance.analytics.domain.ReportType;
import com.lifebalance.analytics.dto.AnalyticsDashboardResponse;
import com.lifebalance.analytics.dto.AnalyticsReportExport;
import com.lifebalance.analytics.dto.AnalyticsReportResponse;
import com.lifebalance.analytics.dto.GenerateReportRequest;
import com.lifebalance.analytics.dto.ReasonRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnalyticsReportService {

    AnalyticsReportResponse generate(UUID ownerId, GenerateReportRequest request);

    AnalyticsReportResponse archive(UUID ownerId, UUID reportId, ReasonRequest request);

    AnalyticsReportResponse getById(UUID ownerId, UUID reportId);

    AnalyticsReportExport export(UUID ownerId, UUID reportId, ReportExportFormat format);

    Page<AnalyticsReportResponse> search(
            UUID ownerId,
            ReportType reportType,
            ReportDimension dimension,
            ReportStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    );

    AnalyticsDashboardResponse dashboard(UUID ownerId, LocalDate periodStart, LocalDate periodEnd, String currencyCode);
}
