package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalBelowAllocatedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalCycleNotAdjustableException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalAllocationReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalAdjustmentServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID HISTORY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-08-04T10:00:00Z");

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Mock
    private TimeCapitalRepository timeCapitalRepository;

    @Mock
    private MoneyCapitalRepository moneyCapitalRepository;

    @Mock
    private CapitalAdjustmentRepository capitalAdjustmentRepository;

    @Mock
    private CapitalHistoryRepository capitalHistoryRepository;

    @Mock
    private CapitalAllocationReader capitalAllocationReader;

    @Test
    void adjustTimeCapitalIncreasesPlannedMinutesAndRecordsHistory() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 120L);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalHistoryRepository.saveAndFlush(any(CapitalHistory.class))).thenAnswer(invocation -> {
            CapitalHistory history = invocation.getArgument(0);
            setField(history, "id", HISTORY_ID);
            return history;
        });

        TimeCapitalAdjustmentResponse response = createService().adjustTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 30L, "Add commute buffer")
        );

        ArgumentCaptor<CapitalHistory> historyCaptor = ArgumentCaptor.forClass(CapitalHistory.class);
        verify(capitalHistoryRepository).saveAndFlush(historyCaptor.capture());
        CapitalHistory history = historyCaptor.getValue();
        assertThat(timeCapital.getPlannedMinutes()).isEqualTo(150L);
        assertThat(response.actionType()).isEqualTo(CapitalActionType.ADJUSTMENT_INCREASE);
        assertThat(response.beforeMinutes()).isEqualTo(120L);
        assertThat(response.afterMinutes()).isEqualTo(150L);
        assertThat(response.historyId()).isEqualTo(HISTORY_ID);
        assertThat(history.getCapitalType()).isEqualTo(CapitalKind.TIME);
        assertThat(history.getActionType()).isEqualTo(CapitalActionType.ADJUSTMENT_INCREASE);
        assertThat(history.getAmount()).isEqualByComparingTo("30.0000");
        assertThat(history.getBeforeAmount()).isEqualByComparingTo("120.0000");
        assertThat(history.getAfterAmount()).isEqualByComparingTo("150.0000");
        assertThat(history.getReason()).isEqualTo("Add commute buffer");
        assertThat(history.getReferenceType()).isEqualTo(CapitalReferenceType.MANUAL);
        assertThat(history.getReferenceId()).isNull();
        assertThat(history.getActorType()).isEqualTo(CapitalActorType.USER);
        assertThat(history.getActorId()).isEqualTo(OWNER_ID);
    }

    @Test
    void adjustTimeCapitalAllowsDecreaseToAllocatedBoundary() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        cycle.activate(NOW.minusSeconds(120));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationReader.getAllocatedMinutes(CYCLE_ID)).thenReturn(60L);
        when(capitalHistoryRepository.saveAndFlush(any(CapitalHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeCapitalAdjustmentResponse response = createService().adjustTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.DECREASE, 40L, "Trim free evening")
        );

        assertThat(timeCapital.getPlannedMinutes()).isEqualTo(60L);
        assertThat(response.actionType()).isEqualTo(CapitalActionType.ADJUSTMENT_DECREASE);
        assertThat(response.afterMinutes()).isEqualTo(60L);
    }

    @Test
    void adjustTimeCapitalRejectsDecreaseBelowAllocatedWithoutMutatingOrWritingHistory() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        cycle.activate(NOW.minusSeconds(120));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationReader.getAllocatedMinutes(CYCLE_ID)).thenReturn(60L);

        assertThatThrownBy(() -> createService().adjustTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.DECREASE, 41L, "Too much")
        )).isInstanceOf(CapitalBelowAllocatedException.class);

        assertThat(timeCapital.getPlannedMinutes()).isEqualTo(100L);
        verify(capitalHistoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void adjustMoneyCapitalIncreasesPlannedAmountAndIncludesCurrencyInResponse() {
        CapitalCycle cycle = reopenedCycle();
        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("100.0000"), "vnd");
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(moneyCapital));
        when(capitalHistoryRepository.saveAndFlush(any(CapitalHistory.class))).thenAnswer(invocation -> {
            CapitalHistory history = invocation.getArgument(0);
            setField(history, "id", HISTORY_ID);
            return history;
        });

        MoneyCapitalAdjustmentResponse response = createService().adjustMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustMoneyCapitalRequest(CapitalAdjustmentType.INCREASE, new BigDecimal("25.5000"), "Top up budget")
        );

        ArgumentCaptor<CapitalHistory> historyCaptor = ArgumentCaptor.forClass(CapitalHistory.class);
        verify(capitalHistoryRepository).saveAndFlush(historyCaptor.capture());
        assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("125.5000");
        assertThat(response.currencyCode()).isEqualTo("VND");
        assertThat(response.beforeAmount()).isEqualByComparingTo("100.0000");
        assertThat(response.afterAmount()).isEqualByComparingTo("125.5000");
        assertThat(historyCaptor.getValue().getAmount()).isEqualByComparingTo("25.5000");
        assertThat(historyCaptor.getValue().getReferenceType()).isEqualTo(CapitalReferenceType.MANUAL);
    }

    @Test
    void adjustMoneyCapitalRejectsDecreaseBelowAllocated() {
        CapitalCycle cycle = draftCycle();
        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("100.0000"), "USD");
        cycle.activate(NOW.minusSeconds(120));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(moneyCapital));
        when(capitalAllocationReader.getAllocatedAmount(CYCLE_ID)).thenReturn(new BigDecimal("75.0000"));

        assertThatThrownBy(() -> createService().adjustMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustMoneyCapitalRequest(CapitalAdjustmentType.DECREASE, new BigDecimal("25.0001"), "Too much")
        )).isInstanceOf(CapitalBelowAllocatedException.class);

        assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("100.0000");
        verify(capitalHistoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void adjustMoneyCapitalAllowsDecreaseToAllocatedBoundary() {
        CapitalCycle cycle = draftCycle();
        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("100.0000"), "USD");
        cycle.activate(NOW.minusSeconds(120));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(moneyCapital));
        when(capitalAllocationReader.getAllocatedAmount(CYCLE_ID)).thenReturn(new BigDecimal("75.0000"));
        when(capitalHistoryRepository.saveAndFlush(any(CapitalHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MoneyCapitalAdjustmentResponse response = createService().adjustMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustMoneyCapitalRequest(CapitalAdjustmentType.DECREASE, new BigDecimal("25.0000"), "Reduce budget")
        );

        assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("75.0000");
        assertThat(response.actionType()).isEqualTo(CapitalActionType.ADJUSTMENT_DECREASE);
        assertThat(response.afterAmount()).isEqualByComparingTo("75.0000");
    }

    @Test
    void adjustTimeCapitalRejectsOverflowWithoutWritingHistory() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, Long.MAX_VALUE);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(timeCapital));

        assertThatThrownBy(() -> createService().adjustTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 1L, "Overflow")
        )).isInstanceOf(InvalidAdjustmentAmountException.class)
                .hasMessageContaining("exceeds");

        assertThat(timeCapital.getPlannedMinutes()).isEqualTo(Long.MAX_VALUE);
        verify(capitalHistoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void adjustMoneyCapitalRejectsPrecisionOverflowWithoutWritingHistory() {
        CapitalCycle cycle = draftCycle();
        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("1.0000"), "USD");
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(moneyCapital));

        assertThatThrownBy(() -> createService().adjustMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustMoneyCapitalRequest(
                        CapitalAdjustmentType.INCREASE,
                        new BigDecimal("1000000000000000.0000"),
                        "Too large"
                )
        )).isInstanceOf(InvalidAdjustmentAmountException.class)
                .hasMessageContaining("integer digits");

        assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("1.0000");
        verify(capitalHistoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void adjustCapitalRejectsClosedCycle() {
        CapitalCycle cycle = closedCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().adjustTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 30L, "Late change")
        )).isInstanceOf(CapitalCycleNotAdjustableException.class);

        verifyNoInteractions(timeCapitalRepository, moneyCapitalRepository, capitalHistoryRepository);
    }

    @Test
    void adjustCapitalRejectsCycleOwnedByAnotherUser() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OTHER_OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createService().adjustTimeCapital(
                OTHER_OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 30L, "No ownership")
        )).isInstanceOf(CapitalCycleNotFoundException.class);

        verifyNoInteractions(timeCapitalRepository, moneyCapitalRepository, capitalHistoryRepository);
    }

    @Test
    void adjustCapitalRejectsInvalidRequestValues() {
        assertThatThrownBy(() -> createService().adjustTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 0L, "zero")
        )).isInstanceOf(InvalidAdjustmentAmountException.class);

        assertThatThrownBy(() -> createService().adjustMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustMoneyCapitalRequest(CapitalAdjustmentType.INCREASE, new BigDecimal("1.00001"), "scale")
        )).isInstanceOf(InvalidAdjustmentAmountException.class);

        assertThatThrownBy(() -> createService().adjustMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new AdjustMoneyCapitalRequest(CapitalAdjustmentType.INCREASE, new BigDecimal("1.0000"), " ")
        )).isInstanceOf(InvalidAdjustmentAmountException.class);
    }

    private CapitalAdjustmentServiceImpl createService() {
        return new CapitalAdjustmentServiceImpl(
                capitalCycleRepository,
                timeCapitalRepository,
                moneyCapitalRepository,
                capitalAdjustmentRepository,
                capitalHistoryRepository,
                capitalAllocationReader
        );
    }

    private static CapitalCycle draftCycle() {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                "August 4",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 4),
                LocalDate.of(2026, 8, 4)
        );
        setField(cycle, "id", CYCLE_ID);
        return cycle;
    }

    private static CapitalCycle activeCycle() {
        CapitalCycle cycle = draftCycle();
        cycle.activate(NOW.minusSeconds(120));
        return cycle;
    }

    private static CapitalCycle closedCycle() {
        CapitalCycle cycle = activeCycle();
        cycle.close("Finished", NOW.minusSeconds(60));
        return cycle;
    }

    private static CapitalCycle reopenedCycle() {
        CapitalCycle cycle = closedCycle();
        cycle.reopen("Need correction", NOW.minusSeconds(30));
        return cycle;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set test field " + fieldName, exception);
        }
    }
}
