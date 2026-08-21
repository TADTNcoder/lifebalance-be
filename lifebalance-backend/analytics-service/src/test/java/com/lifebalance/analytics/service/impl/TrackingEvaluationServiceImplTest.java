package com.lifebalance.analytics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.domain.TrendGranularity;
import com.lifebalance.analytics.dto.EvaluationTrendPointResponse;
import com.lifebalance.analytics.dto.TrackingEvaluationSummaryResponse;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TrackingEvaluationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ONE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_TWO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private ActualRecordRepository actualRecordRepository;

    @Mock
    private EvaluationResultRepository evaluationResultRepository;

    @Test
    void summaryAggregatesTrackingAndEvaluationKpis() {
        LocalDate from = LocalDate.parse("2026-08-01");
        LocalDate to = LocalDate.parse("2026-08-31");
        when(evaluationResultRepository.findActiveByOwnerAndPeriod(OWNER_ID, from, to))
                .thenReturn(List.of(
                        evaluation(TASK_ONE_ID, from, to, 60, 90, new BigDecimal("100.0000"),
                                new BigDecimal("120.0000"), "USD", EvaluationStatus.OVER_PLANNED,
                                new BigDecimal("75.0000")),
                        evaluation(TASK_TWO_ID, from, to, 120, 60, null, null, null,
                                EvaluationStatus.UNDER_PLANNED, new BigDecimal("200.0000"))
                ));
        when(actualRecordRepository.countActiveRecords(OWNER_ID, from, to)).thenReturn(3L);

        TrackingEvaluationSummaryResponse response = service().summary(OWNER_ID, from, to, "usd");

        assertThat(response.evaluatedTaskCount()).isEqualTo(2);
        assertThat(response.actualRecordCount()).isEqualTo(3);
        assertThat(response.overPlannedCount()).isEqualTo(1);
        assertThat(response.underPlannedCount()).isEqualTo(1);
        assertThat(response.plannedMinutes()).isEqualTo(180);
        assertThat(response.actualMinutes()).isEqualTo(150);
        assertThat(response.minuteVariance()).isEqualTo(-30);
        assertThat(response.plannedCost()).isEqualByComparingTo("100.0000");
        assertThat(response.actualCost()).isEqualByComparingTo("120.0000");
        assertThat(response.costVariance()).isEqualByComparingTo("20.0000");
        assertThat(response.timeEfficiencyPercent()).isEqualByComparingTo("120.0000");
        assertThat(response.costEfficiencyPercent()).isEqualByComparingTo("83.3333");
        assertThat(response.overallEfficiencyPercent()).isEqualByComparingTo("101.6667");
        assertThat(response.planningAccuracyPercent()).isEqualByComparingTo("81.6667");
        assertThat(response.dataCompletenessPercent()).isEqualByComparingTo("100.0000");
        assertThat(response.productivityScore()).isEqualByComparingTo("93.8889");
    }

    @Test
    void trendReturnsMonthlyBucketsIncludingEmptyPeriods() {
        LocalDate from = LocalDate.parse("2026-08-01");
        LocalDate to = LocalDate.parse("2026-09-30");
        when(evaluationResultRepository.findActiveByOwnerAndPeriod(OWNER_ID, from, to))
                .thenReturn(List.of(evaluation(TASK_ONE_ID, from, LocalDate.parse("2026-08-31"),
                        60, 90, null, null, null, EvaluationStatus.OVER_PLANNED, new BigDecimal("66.6667"))));
        when(actualRecordRepository.countActiveRecords(
                OWNER_ID,
                LocalDate.parse("2026-08-01"),
                LocalDate.parse("2026-08-31")
        )).thenReturn(1L);
        when(actualRecordRepository.countActiveRecords(
                OWNER_ID,
                LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-09-30")
        )).thenReturn(0L);

        List<EvaluationTrendPointResponse> trend = service().trend(
                OWNER_ID,
                from,
                to,
                TrendGranularity.MONTHLY,
                null
        );

        assertThat(trend).hasSize(2);
        assertThat(trend.get(0).periodStart()).isEqualTo(LocalDate.parse("2026-08-01"));
        assertThat(trend.get(0).periodEnd()).isEqualTo(LocalDate.parse("2026-08-31"));
        assertThat(trend.get(0).actualRecordCount()).isEqualTo(1);
        assertThat(trend.get(0).plannedMinutes()).isEqualTo(60);
        assertThat(trend.get(1).periodStart()).isEqualTo(LocalDate.parse("2026-09-01"));
        assertThat(trend.get(1).periodEnd()).isEqualTo(LocalDate.parse("2026-09-30"));
        assertThat(trend.get(1).evaluatedTaskCount()).isZero();
        assertThat(trend.get(1).plannedMinutes()).isNull();
    }

    private TrackingEvaluationServiceImpl service() {
        return new TrackingEvaluationServiceImpl(actualRecordRepository, evaluationResultRepository);
    }

    private static EvaluationResult evaluation(
            UUID taskId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Integer plannedMinutes,
            Integer actualMinutes,
            BigDecimal plannedCost,
            BigDecimal actualCost,
            String currencyCode,
            EvaluationStatus status,
            BigDecimal efficiencyPercent
    ) {
        EvaluationResult result = EvaluationResult.create(
                OWNER_ID,
                OWNER_ID,
                taskId,
                null,
                periodStart,
                periodEnd,
                plannedMinutes,
                actualMinutes,
                plannedMinutes == null || actualMinutes == null ? null : actualMinutes - plannedMinutes,
                plannedCost,
                actualCost,
                plannedCost == null || actualCost == null ? null : actualCost.subtract(plannedCost),
                currencyCode,
                efficiencyPercent,
                status,
                "test"
        );
        ReflectionTestUtils.setField(result, "id", UUID.randomUUID());
        return result;
    }
}
