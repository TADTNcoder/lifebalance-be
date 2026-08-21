package com.lifebalance.analytics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.analytics.config.AnalyticsExportProperties;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.domain.AnalyticsReport;
import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportExportFormat;
import com.lifebalance.analytics.domain.ReportType;
import com.lifebalance.analytics.dto.AnalyticsReportExport;
import com.lifebalance.analytics.dto.AnalyticsReportResponse;
import com.lifebalance.analytics.dto.GenerateReportRequest;
import com.lifebalance.analytics.error.AnalyticsErrorCode;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.AnalyticsReportRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import com.lifebalance.common.error.AppException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnalyticsReportServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID REPORT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private ActualRecordRepository actualRecordRepository;

    @Mock
    private EvaluationResultRepository evaluationResultRepository;

    @Mock
    private AnalyticsReportRepository reportRepository;

    @Mock
    private AnalyticsHistoryRecorder historyRecorder;

    @Test
    void generateReportAggregatesPeriodAndWritesHistory() {
        LocalDate from = LocalDate.parse("2026-08-01");
        LocalDate to = LocalDate.parse("2026-08-31");
        when(actualRecordRepository.sumActualMinutes(OWNER_ID, null, null, from, to)).thenReturn(300L);
        when(actualRecordRepository.sumActualCost(OWNER_ID, null, null, "USD", from, to))
                .thenReturn(new BigDecimal("125.5000"));
        when(actualRecordRepository.countDistinctActiveTasks(OWNER_ID, from, to)).thenReturn(4L);
        when(actualRecordRepository.countActiveRecords(OWNER_ID, from, to)).thenReturn(7L);
        when(evaluationResultRepository.findEfficiencyPercentages(OWNER_ID, from, to))
                .thenReturn(List.of(new BigDecimal("90.0000"), new BigDecimal("110.0000")));
        when(reportRepository.save(any(AnalyticsReport.class))).thenAnswer(invocation -> {
            AnalyticsReport report = invocation.getArgument(0);
            ReflectionTestUtils.setField(report, "id", REPORT_ID);
            return report;
        });

        AnalyticsReportResponse response = createService().generate(OWNER_ID, new GenerateReportRequest(
                ReportType.SUMMARY,
                ReportDimension.PERIOD,
                from,
                to,
                "usd",
                "Monthly analytics"
        ));

        assertThat(response.id()).isEqualTo(REPORT_ID);
        assertThat(response.taskCount()).isEqualTo(4);
        assertThat(response.actualRecordCount()).isEqualTo(7);
        assertThat(response.totalActualMinutes()).isEqualTo(300);
        assertThat(response.totalActualCost()).isEqualByComparingTo("125.5000");
        assertThat(response.currencyCode()).isEqualTo("USD");
        assertThat(response.averageEfficiencyPercent()).isEqualByComparingTo("100.0000");
        ArgumentCaptor<AnalyticsReport> captor = ArgumentCaptor.forClass(AnalyticsReport.class);
        verify(reportRepository).save(captor.capture());
        verify(historyRecorder).recordReport(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AnalyticsHistoryActionType.REPORT_GENERATED),
                eq(captor.getValue()),
                isNull(),
                contains("actualRecords=7"),
                eq("Monthly analytics")
        );
    }

    @Test
    void exportCsvUsesStoredReportAndWritesHistory() {
        AnalyticsReport report = report();
        when(reportRepository.findByIdAndOwnerId(REPORT_ID, OWNER_ID)).thenReturn(Optional.of(report));

        AnalyticsReportExport export = createService().export(OWNER_ID, REPORT_ID, ReportExportFormat.CSV);

        assertThat(export.filename()).isEqualTo("lifebalance-report-" + REPORT_ID + ".csv");
        assertThat(export.contentType()).isEqualTo("text/csv");
        assertThat(new String(export.content(), StandardCharsets.UTF_8))
                .contains("\"Report Type\",\"SUMMARY\"")
                .contains("\"Actual Record Count\",\"7\"")
                .contains("\"Average Efficiency Percent\",\"100.0000\"");
        verify(historyRecorder).recordReport(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AnalyticsHistoryActionType.REPORT_EXPORTED),
                eq(report),
                isNull(),
                contains("format=CSV"),
                eq("Report exported")
        );
    }

    @Test
    void exportRejectsFormatWhenPolicyDoesNotApproveIt() {
        AnalyticsExportProperties properties = new AnalyticsExportProperties();
        properties.setAllowedFormats(Set.of(ReportExportFormat.CSV));

        assertThatThrownBy(() -> createService(properties).export(OWNER_ID, REPORT_ID, ReportExportFormat.PDF))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(AnalyticsErrorCode.INVALID_REQUEST);

        verify(reportRepository, never()).findByIdAndOwnerId(any(), any());
    }

    @Test
    void exportCreatesExcelAndPdfBinaryFiles() {
        AnalyticsReport report = report();
        when(reportRepository.findByIdAndOwnerId(REPORT_ID, OWNER_ID)).thenReturn(Optional.of(report));

        AnalyticsReportExport excel = createService().export(OWNER_ID, REPORT_ID, ReportExportFormat.EXCEL);
        AnalyticsReportExport pdf = createService().export(OWNER_ID, REPORT_ID, ReportExportFormat.PDF);

        assertThat(excel.contentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(excel.content()).startsWith(new byte[] {'P', 'K'});
        assertThat(pdf.contentType()).isEqualTo("application/pdf");
        assertThat(new String(pdf.content(), 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    private AnalyticsReportServiceImpl createService() {
        return createService(new AnalyticsExportProperties());
    }

    private AnalyticsReportServiceImpl createService(AnalyticsExportProperties exportProperties) {
        return new AnalyticsReportServiceImpl(
                actualRecordRepository,
                evaluationResultRepository,
                reportRepository,
                historyRecorder,
                new AnalyticsMapper(),
                new AnalyticsReportExporter(),
                exportProperties
        );
    }

    private static AnalyticsReport report() {
        AnalyticsReport report = AnalyticsReport.generate(
                OWNER_ID,
                OWNER_ID,
                ReportType.SUMMARY,
                ReportDimension.PERIOD,
                LocalDate.parse("2026-08-01"),
                LocalDate.parse("2026-08-31"),
                4,
                7,
                300,
                new BigDecimal("125.5000"),
                "USD",
                new BigDecimal("100.0000"),
                "actualRecordCount=7;taskCount=4;totalActualMinutes=300",
                "Monthly analytics"
        );
        ReflectionTestUtils.setField(report, "id", REPORT_ID);
        return report;
    }
}
