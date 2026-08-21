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

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.ActualRecordType;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.dto.ActualRecordResponse;
import com.lifebalance.analytics.dto.RecordActualRequest;
import com.lifebalance.analytics.error.AnalyticsErrorCode;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.common.error.AppException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ActualRecordServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACTUAL_RECORD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private ActualRecordRepository actualRecordRepository;

    @Mock
    private AnalyticsHistoryRecorder historyRecorder;

    @Test
    void recordTimeActualPersistsAndWritesHistory() {
        when(actualRecordRepository.save(any(ActualRecord.class))).thenAnswer(invocation -> {
            ActualRecord actualRecord = invocation.getArgument(0);
            ReflectionTestUtils.setField(actualRecord, "id", ACTUAL_RECORD_ID);
            return actualRecord;
        });

        ActualRecordResponse response = createService().record(OWNER_ID, new RecordActualRequest(
                ActualRecordType.TIME,
                TASK_ID,
                null,
                null,
                Set.of(),
                90,
                null,
                null,
                LocalDate.parse("2026-08-21"),
                "Deep work",
                "manual"
        ));

        assertThat(response.id()).isEqualTo(ACTUAL_RECORD_ID);
        assertThat(response.actualMinutes()).isEqualTo(90);
        assertThat(response.actualCost()).isNull();
        ArgumentCaptor<ActualRecord> captor = ArgumentCaptor.forClass(ActualRecord.class);
        verify(actualRecordRepository).save(captor.capture());
        verify(historyRecorder).recordActual(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AnalyticsHistoryActionType.ACTUAL_RECORDED),
                eq(captor.getValue()),
                isNull(),
                contains("minutes=90"),
                eq("Deep work")
        );
    }

    @Test
    void recordTimeActualAllowsZeroConsumption() {
        when(actualRecordRepository.save(any(ActualRecord.class))).thenAnswer(invocation -> {
            ActualRecord actualRecord = invocation.getArgument(0);
            ReflectionTestUtils.setField(actualRecord, "id", ACTUAL_RECORD_ID);
            return actualRecord;
        });

        ActualRecordResponse response = createService().record(OWNER_ID, new RecordActualRequest(
                ActualRecordType.TIME,
                TASK_ID,
                null,
                null,
                Set.of(),
                0,
                null,
                null,
                LocalDate.parse("2026-08-21"),
                "Completed without measurable time",
                "manual"
        ));

        assertThat(response.actualMinutes()).isZero();
        verify(historyRecorder).recordActual(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AnalyticsHistoryActionType.ACTUAL_RECORDED),
                any(ActualRecord.class),
                isNull(),
                contains("minutes=0"),
                eq("Completed without measurable time")
        );
    }

    @Test
    void recordMoneyActualRejectsMissingCurrencyBeforeSaving() {
        assertThatThrownBy(() -> createService().record(OWNER_ID, new RecordActualRequest(
                ActualRecordType.MONEY,
                TASK_ID,
                null,
                null,
                Set.of(),
                null,
                BigDecimal.TEN,
                null,
                LocalDate.parse("2026-08-21"),
                null,
                "manual"
        )))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(AnalyticsErrorCode.INVALID_CURRENCY);

        verify(actualRecordRepository, never()).save(any());
        verify(historyRecorder, never()).recordActual(any(), any(), any(), any(), any(), any(), any());
    }

    private ActualRecordServiceImpl createService() {
        return new ActualRecordServiceImpl(
                actualRecordRepository,
                historyRecorder,
                new AnalyticsMapper()
        );
    }
}
