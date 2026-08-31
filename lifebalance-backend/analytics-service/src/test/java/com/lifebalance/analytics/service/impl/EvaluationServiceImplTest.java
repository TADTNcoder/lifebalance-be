package com.lifebalance.analytics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.dto.EvaluateTaskRequest;
import com.lifebalance.analytics.dto.EvaluationResultResponse;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID EVALUATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private ActualRecordRepository actualRecordRepository;

    @Mock
    private EvaluationResultRepository evaluationResultRepository;

    @Mock
    private AnalyticsHistoryRecorder historyRecorder;

    @Test
    void evaluateTaskAggregatesActualMinutesAndMarksOverPlanned() {
        when(actualRecordRepository.sumActualMinutes(
                eq(OWNER_ID),
                eq(TASK_ID),
                isNull(),
                eq(LocalDate.parse("2026-08-01")),
                eq(LocalDate.parse("2026-08-31"))
        )).thenReturn(90L);
        when(evaluationResultRepository.save(any(EvaluationResult.class))).thenAnswer(invocation -> {
            EvaluationResult result = invocation.getArgument(0);
            ReflectionTestUtils.setField(result, "id", EVALUATION_ID);
            return result;
        });

        EvaluationResultResponse response = createService().evaluateTask(OWNER_ID, new EvaluateTaskRequest(
                TASK_ID,
                null,
                LocalDate.parse("2026-08-01"),
                LocalDate.parse("2026-08-31"),
                60,
                null,
                null,
                null,
                null,
                "Monthly review"
        ));

        assertThat(response.status()).isEqualTo(EvaluationStatus.OVER_PLANNED);
        assertThat(response.actualMinutes()).isEqualTo(90);
        assertThat(response.minuteVariance()).isEqualTo(30);
        assertThat(response.efficiencyPercent()).isEqualByComparingTo("66.6667");
        ArgumentCaptor<EvaluationResult> captor = ArgumentCaptor.forClass(EvaluationResult.class);
        verify(evaluationResultRepository).save(captor.capture());
        verify(historyRecorder).recordEvaluation(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AnalyticsHistoryActionType.EVALUATION_GENERATED),
                eq(captor.getValue()),
                isNull(),
                contains("status=OVER_PLANNED"),
                eq("Monthly review")
        );
    }

    @Test
    void searchReplacesMissingTimestampBoundsBeforeQueryingPostgres() {
        Pageable pageable = PageRequest.of(0, 20);
        when(evaluationResultRepository.search(
                eq(OWNER_ID),
                isNull(),
                isNull(),
                isNull(),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                eq(pageable)
        )).thenReturn(Page.empty(pageable));

        createService().search(OWNER_ID, null, null, null, null, null, pageable);

        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(evaluationResultRepository).search(
                eq(OWNER_ID),
                isNull(),
                isNull(),
                isNull(),
                fromCaptor.capture(),
                toCaptor.capture(),
                eq(pageable)
        );
        assertThat(fromCaptor.getValue()).isEqualTo(OffsetDateTime.parse("0001-01-01T00:00:00Z"));
        assertThat(toCaptor.getValue()).isEqualTo(OffsetDateTime.parse("9999-12-31T23:59:59.999999999Z"));
    }

    private EvaluationServiceImpl createService() {
        return new EvaluationServiceImpl(
                actualRecordRepository,
                evaluationResultRepository,
                historyRecorder,
                new AnalyticsMapper()
        );
    }
}
