package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.config.AnalyticsExportProperties;
import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.domain.AnalyticsReport;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportExportFormat;
import com.lifebalance.analytics.domain.ReportStatus;
import com.lifebalance.analytics.domain.ReportType;
import com.lifebalance.analytics.dto.AnalyticsDashboardResponse;
import com.lifebalance.analytics.dto.AnalyticsReportExport;
import com.lifebalance.analytics.dto.AnalyticsReportResponse;
import com.lifebalance.analytics.dto.GenerateReportRequest;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.error.AnalyticsExceptions;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.AnalyticsReportRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import com.lifebalance.analytics.service.AnalyticsReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AnalyticsReportServiceImpl implements AnalyticsReportService {

    private final ActualRecordRepository actualRecordRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final AnalyticsReportRepository reportRepository;
    private final AnalyticsHistoryRecorder historyRecorder;
    private final AnalyticsMapper mapper;
    private final AnalyticsReportExporter reportExporter;
    private final AnalyticsExportProperties exportProperties;

    AnalyticsReportServiceImpl(
            ActualRecordRepository actualRecordRepository,
            EvaluationResultRepository evaluationResultRepository,
            AnalyticsReportRepository reportRepository,
            AnalyticsHistoryRecorder historyRecorder,
            AnalyticsMapper mapper,
            AnalyticsReportExporter reportExporter,
            AnalyticsExportProperties exportProperties
    ) {
        this.actualRecordRepository = actualRecordRepository;
        this.evaluationResultRepository = evaluationResultRepository;
        this.reportRepository = reportRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
        this.reportExporter = reportExporter;
        this.exportProperties = exportProperties;
    }

    @Override
    @Transactional
    public AnalyticsReportResponse generate(UUID ownerId, GenerateReportRequest request) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(request.periodStart(), request.periodEnd());

        String currencyCode = normalizeCurrencyOrNull(request.currencyCode());
        Aggregates aggregates = aggregates(ownerId, request.periodStart(), request.periodEnd(), currencyCode);
        AnalyticsReport report = AnalyticsReport.generate(
                ownerId,
                ownerId,
                request.reportType(),
                request.dimension(),
                request.periodStart(),
                request.periodEnd(),
                aggregates.taskCount(),
                aggregates.actualRecordCount(),
                aggregates.totalMinutes(),
                aggregates.totalCost(),
                currencyCode,
                aggregates.averageEfficiencyPercent(),
                varianceSummary(aggregates, currencyCode),
                request.reason()
        );

        AnalyticsReport saved = reportRepository.save(report);
        historyRecorder.recordReport(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.REPORT_GENERATED,
                saved,
                null,
                mapper.reportSnapshot(saved),
                request.reason()
        );
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AnalyticsReportResponse archive(UUID ownerId, UUID reportId, ReasonRequest request) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        AnalyticsReport report = reportRepository.findByIdAndOwnerIdForUpdate(reportId, ownerId)
                .orElseThrow(() -> AnalyticsExceptions.reportNotFound(reportId));
        String oldSnapshot = mapper.reportSnapshot(report);
        report.archive(ownerId);

        historyRecorder.recordReport(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.REPORT_ARCHIVED,
                report,
                oldSnapshot,
                mapper.reportSnapshot(report),
                request == null ? null : request.reason()
        );
        return mapper.toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsReportResponse getById(UUID ownerId, UUID reportId) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        return reportRepository.findByIdAndOwnerId(reportId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> AnalyticsExceptions.reportNotFound(reportId));
    }

    @Override
    @Transactional
    public AnalyticsReportExport export(UUID ownerId, UUID reportId, ReportExportFormat format) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        if (!exportProperties.isAllowed(format)) {
            throw AnalyticsExceptions.invalidRequest("export format is not approved by policy.");
        }
        AnalyticsReport report = reportRepository.findByIdAndOwnerId(reportId, ownerId)
                .orElseThrow(() -> AnalyticsExceptions.reportNotFound(reportId));
        AnalyticsReportExport export = reportExporter.export(mapper.toResponse(report), format);
        if (exportProperties.isAuditEnabled()) {
            historyRecorder.recordReport(
                    ownerId,
                    ownerId,
                    AnalyticsHistoryActionType.REPORT_EXPORTED,
                    report,
                    null,
                    "format=%s;filename=%s".formatted(format, export.filename()),
                    "Report exported"
            );
        }
        return export;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnalyticsReportResponse> search(
            UUID ownerId,
            ReportType reportType,
            ReportDimension dimension,
            ReportStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(periodStart, periodEnd);
        return reportRepository.search(ownerId, reportType, dimension, status, periodStart, periodEnd, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse dashboard(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String currencyCode
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(periodStart, periodEnd);
        String normalizedCurrency = normalizeCurrencyOrNull(currencyCode);
        Aggregates aggregates = aggregates(ownerId, periodStart, periodEnd, normalizedCurrency);
        return new AnalyticsDashboardResponse(
                ownerId,
                periodStart,
                periodEnd,
                aggregates.actualRecordCount(),
                evaluationResultRepository.countEvaluatedTasks(ownerId, periodStart, periodEnd),
                aggregates.totalMinutes(),
                aggregates.totalCost(),
                normalizedCurrency,
                aggregates.averageEfficiencyPercent(),
                evaluationResultRepository.countByOwnerStatusAndPeriod(ownerId, EvaluationStatus.OVER_PLANNED, periodStart, periodEnd),
                evaluationResultRepository.countByOwnerStatusAndPeriod(ownerId, EvaluationStatus.UNDER_PLANNED, periodStart, periodEnd),
                evaluationResultRepository.countByOwnerStatusAndPeriod(ownerId, EvaluationStatus.ON_TRACK, periodStart, periodEnd)
        );
    }

    private Aggregates aggregates(UUID ownerId, LocalDate periodStart, LocalDate periodEnd, String currencyCode) {
        Long totalMinutes = actualRecordRepository.sumActualMinutes(ownerId, null, null, periodStart, periodEnd);
        BigDecimal totalCost = currencyCode == null
                ? null
                : actualRecordRepository.sumActualCost(ownerId, null, null, currencyCode, periodStart, periodEnd);
        BigDecimal averageEfficiencyPercent = EvaluationServiceImpl.averageEfficiencyPercent(
                evaluationResultRepository.findEfficiencyPercentages(ownerId, periodStart, periodEnd)
        );
        return new Aggregates(
                Math.toIntExact(actualRecordRepository.countDistinctActiveTasks(ownerId, periodStart, periodEnd)),
                Math.toIntExact(actualRecordRepository.countActiveRecords(ownerId, periodStart, periodEnd)),
                totalMinutes == null ? 0 : Math.toIntExact(totalMinutes),
                totalCost == null && currencyCode != null ? BigDecimal.ZERO : totalCost,
                averageEfficiencyPercent
        );
    }

    private static String normalizeCurrencyOrNull(String currencyCode) {
        if (ActualRecord.normalizeText(currencyCode, 3) == null) {
            return null;
        }
        return ActualRecord.normalizeCurrency(currencyCode);
    }

    private static String varianceSummary(Aggregates aggregates, String currencyCode) {
        return "actualRecordCount=%d;taskCount=%d;totalActualMinutes=%d;totalActualCost=%s;currencyCode=%s;averageEfficiencyPercent=%s"
                .formatted(
                        aggregates.actualRecordCount(),
                        aggregates.taskCount(),
                        aggregates.totalMinutes(),
                        aggregates.totalCost(),
                        currencyCode,
                        aggregates.averageEfficiencyPercent()
                );
    }

    private record Aggregates(
            int taskCount,
            int actualRecordCount,
            int totalMinutes,
            BigDecimal totalCost,
            BigDecimal averageEfficiencyPercent
    ) {
    }
}
