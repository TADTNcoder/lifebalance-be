package com.lifebalance.analytics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.dto.AutomaticEvaluationBaselineRequest;
import com.lifebalance.analytics.dto.EvaluateTaskRequest;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import com.lifebalance.analytics.service.EvaluationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutomaticEvaluationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private ActualRecordRepository actualRecordRepository;

    @Mock
    private EvaluationResultRepository evaluationResultRepository;

    @Test
    void buildsEvaluationFromRequestedPlanAndAggregatedActuals() {
        LocalDate actualDate = LocalDate.parse("2026-08-21");
        when(evaluationResultRepository.findFirstByOwnerIdAndTaskIdAndStatusNotOrderByGeneratedAtDesc(
                OWNER_ID, TASK_ID, EvaluationStatus.ARCHIVED
        )).thenReturn(Optional.empty());
        when(actualRecordRepository.sumActualMinutes(
                OWNER_ID, TASK_ID, null, LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31")
        )).thenReturn(90L);
        when(actualRecordRepository.sumActualCost(
                OWNER_ID, TASK_ID, null, "VND", LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31")
        )).thenReturn(new BigDecimal("120000"));

        AutomaticEvaluationService service = new AutomaticEvaluationServiceImpl(
                evaluationService,
                actualRecordRepository,
                evaluationResultRepository
        );
        service.evaluateAfterActualChange(
                OWNER_ID,
                new AutomaticEvaluationTarget(TASK_ID, null, actualDate, "VND", true, true, true),
                new AutomaticEvaluationBaselineRequest(
                        LocalDate.parse("2026-08-01"),
                        LocalDate.parse("2026-08-31"),
                        60,
                        new BigDecimal("100000"),
                        "VND"
                ),
                "Đã hoàn thành"
        );

        ArgumentCaptor<EvaluateTaskRequest> requestCaptor = ArgumentCaptor.forClass(EvaluateTaskRequest.class);
        verify(evaluationService).evaluateTask(eq(OWNER_ID), requestCaptor.capture());
        EvaluateTaskRequest request = requestCaptor.getValue();
        assertThat(request.taskId()).isEqualTo(TASK_ID);
        assertThat(request.periodStart()).isEqualTo(LocalDate.parse("2026-08-01"));
        assertThat(request.periodEnd()).isEqualTo(LocalDate.parse("2026-08-31"));
        assertThat(request.plannedMinutes()).isEqualTo(60);
        assertThat(request.actualMinutes()).isEqualTo(90);
        assertThat(request.plannedCost()).isEqualByComparingTo("100000");
        assertThat(request.actualCost()).isEqualByComparingTo("120000");
        assertThat(request.currencyCode()).isEqualTo("VND");
    }

    @Test
    void reusesLatestPlanWhenLegacyClientDoesNotSendBaseline() {
        EvaluationResult previous = EvaluationResult.create(
                OWNER_ID,
                OWNER_ID,
                TASK_ID,
                null,
                LocalDate.parse("2026-08-01"),
                LocalDate.parse("2026-08-31"),
                60,
                45,
                -15,
                new BigDecimal("100000"),
                new BigDecimal("80000"),
                new BigDecimal("-20000"),
                "VND",
                new BigDecimal("133.3333"),
                EvaluationStatus.UNDER_PLANNED,
                "previous"
        );
        when(evaluationResultRepository.findFirstByOwnerIdAndTaskIdAndStatusNotOrderByGeneratedAtDesc(
                OWNER_ID, TASK_ID, EvaluationStatus.ARCHIVED
        )).thenReturn(Optional.of(previous));
        when(actualRecordRepository.sumActualMinutes(
                OWNER_ID, TASK_ID, null, LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31")
        )).thenReturn(60L);
        when(actualRecordRepository.sumActualCost(
                OWNER_ID, TASK_ID, null, "VND", LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31")
        )).thenReturn(new BigDecimal("100000"));

        AutomaticEvaluationService service = new AutomaticEvaluationServiceImpl(
                evaluationService,
                actualRecordRepository,
                evaluationResultRepository
        );
        service.evaluateAfterActualChange(
                OWNER_ID,
                new AutomaticEvaluationTarget(
                        TASK_ID, null, LocalDate.parse("2026-08-22"), "VND", true, true, true
                ),
                null,
                null
        );

        ArgumentCaptor<EvaluateTaskRequest> requestCaptor = ArgumentCaptor.forClass(EvaluateTaskRequest.class);
        verify(evaluationService).evaluateTask(eq(OWNER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().plannedMinutes()).isEqualTo(60);
        assertThat(requestCaptor.getValue().plannedCost()).isEqualByComparingTo("100000");
        assertThat(requestCaptor.getValue().reason()).contains("tự động");
    }
}
