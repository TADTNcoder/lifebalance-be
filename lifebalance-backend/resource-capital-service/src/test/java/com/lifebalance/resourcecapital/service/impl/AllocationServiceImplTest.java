package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAllocatedCapitalException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationNotAllowedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.AllocationTargetValidator;
import com.lifebalance.resourcecapital.service.DefaultAllocationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SOURCE_TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID DESTINATION_TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Mock
    private TimeCapitalRepository timeCapitalRepository;

    @Mock
    private MoneyCapitalRepository moneyCapitalRepository;

    @Mock
    private CapitalAllocationRepository capitalAllocationRepository;

    @Mock
    private CapitalHistoryRepository capitalHistoryRepository;

    @Mock
    private AllocationTargetValidator allocationTargetValidator;

    @Test
    void allocateCapitalCreatesTargetAllocationAndAllocateHistory() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 120L);
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).thenReturn(Optional.empty());
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("30.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        )).thenReturn(new BigDecimal("30.0000"));
        when(capitalAllocationRepository.saveAndFlush(any(CapitalAllocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubHistoryIds();

        AllocationResponse response = createService().allocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("30.0000"),
                        false,
                        "Plan task"
                )
        );

        ArgumentCaptor<CapitalAllocation> allocationCaptor = ArgumentCaptor.forClass(CapitalAllocation.class);
        ArgumentCaptor<CapitalHistory> historyCaptor = ArgumentCaptor.forClass(CapitalHistory.class);
        verify(capitalAllocationRepository).saveAndFlush(allocationCaptor.capture());
        verify(capitalHistoryRepository).saveAndFlush(historyCaptor.capture());
        assertThat(allocationCaptor.getValue().getAllocatedAmount()).isEqualByComparingTo("30.0000");
        assertThat(historyCaptor.getValue().getActionType()).isEqualTo(CapitalActionType.ALLOCATE);
        assertThat(historyCaptor.getValue().getReferenceType()).isEqualTo(CapitalReferenceType.TASK);
        assertThat(historyCaptor.getValue().getBeforeAmount()).isEqualByComparingTo("0.0000");
        assertThat(historyCaptor.getValue().getAfterAmount()).isEqualByComparingTo("30.0000");
        assertThat(response.totalAllocatedAmount()).isEqualByComparingTo("60.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("60.0000");
        assertThat(response.overAllocated()).isFalse();
    }

    @Test
    void allocateCapitalRejectsOverAllocationWithoutExplicitConfirmation() {
        CapitalCycle cycle = draftCycle();
        cycle.allowOverAllocation();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).thenReturn(Optional.empty());
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("90.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        )).thenReturn(new BigDecimal("90.0000"));

        assertThatThrownBy(() -> createService().allocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("20.0000"),
                        false,
                        "Needs confirmation"
                )
        )).isInstanceOf(OverAllocationConfirmationRequiredException.class)
                .satisfies(exception -> assertThat((OverAllocationConfirmationRequiredException) exception)
                        .extracting(OverAllocationConfirmationRequiredException::getCode)
                        .isEqualTo(OverAllocationConfirmationRequiredException.ERROR_CODE));

        verify(capitalAllocationRepository, never()).saveAndFlush(any());
        verifyNoInteractions(capitalHistoryRepository);
    }

    @Test
    void allocateCapitalRejectsWhenRequestedAmountExceedsAvailableAfterSpentWithoutMutatingAllocation() {
        CapitalCycle cycle = draftCycle();
        cycle.allowOverAllocation();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        CapitalAllocation existing = CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID,
                new BigDecimal("20.0000")
        );
        existing.spend(new BigDecimal("5.0000"));
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).thenReturn(Optional.of(existing));
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("95.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        )).thenReturn(new BigDecimal("95.0000"));
        when(capitalAllocationRepository.sumSpentAmount(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        )).thenReturn(new BigDecimal("5.0000"));

        assertThatThrownBy(() -> createService().allocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("1.0000"),
                        false,
                        "Should fail before mutation"
                )
        )).isInstanceOf(OverAllocationConfirmationRequiredException.class);

        assertThat(existing.getAllocatedAmount()).isEqualByComparingTo("20.0000");
        assertThat(existing.getSpentAmount()).isEqualByComparingTo("5.0000");
        assertThat(existing.getNote()).isNull();
        assertThat(existing.getStatus()).isEqualTo(AllocationStatus.ACTIVE);
        verify(capitalAllocationRepository, never()).saveAndFlush(any());
        verifyNoInteractions(capitalHistoryRepository);
    }

    @Test
    void allocateCapitalRejectsOverAllocationWhenCyclePolicyDisallows() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).thenReturn(Optional.empty());
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("90.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        )).thenReturn(new BigDecimal("90.0000"));

        assertThatThrownBy(() -> createService().allocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("20.0000"),
                        true,
                        "Policy denied"
                )
        )).isInstanceOf(OverAllocationNotAllowedException.class);

        verify(capitalAllocationRepository, never()).saveAndFlush(any());
        verifyNoInteractions(capitalHistoryRepository);
    }

    @Test
    void allocateCapitalWithApprovedOverAllocationWritesAllocateAndApprovalHistory() {
        CapitalCycle cycle = draftCycle();
        cycle.allowOverAllocation();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        CapitalAllocation existing = CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID,
                new BigDecimal("10.0000")
        );
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).thenReturn(Optional.of(existing));
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("90.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        )).thenReturn(new BigDecimal("90.0000"));
        when(capitalAllocationRepository.saveAndFlush(any(CapitalAllocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubHistoryIds();

        AllocationResponse response = createService().allocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("20.0000"),
                        true,
                        "Approved"
                )
        );

        ArgumentCaptor<CapitalHistory> historyCaptor = ArgumentCaptor.forClass(CapitalHistory.class);
        verify(capitalHistoryRepository, org.mockito.Mockito.times(2)).saveAndFlush(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(CapitalHistory::getActionType)
                .containsExactly(CapitalActionType.ALLOCATE, CapitalActionType.OVER_ALLOCATION_APPROVED);
        assertThat(historyCaptor.getAllValues().get(1).getBeforeAmount()).isEqualByComparingTo("10.0000");
        assertThat(historyCaptor.getAllValues().get(1).getAfterAmount()).isEqualByComparingTo("-10.0000");
        assertThat(existing.getAllocatedAmount()).isEqualByComparingTo("30.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("-10.0000");
        assertThat(response.overAllocated()).isTrue();
        assertThat(response.historyIds()).hasSize(2);
    }

    @Test
    void reallocateCapitalMovesAmountBetweenTargetsAndWritesTwoHistoryRecords() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 200L);
        CapitalAllocation source = CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID,
                new BigDecimal("100.0000")
        );
        CapitalAllocation destination = CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                DESTINATION_TASK_ID,
                new BigDecimal("20.0000")
        );
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetsForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                List.of(SOURCE_TASK_ID, DESTINATION_TASK_ID).stream().sorted().toList()
        )).thenReturn(List.of(source, destination));
        when(capitalAllocationRepository.saveAndFlush(destination)).thenReturn(destination);
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("120.0000"));
        stubHistoryIds();

        AllocationResponse response = createService().reallocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new ReallocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        AllocationTargetType.TASK,
                        DESTINATION_TASK_ID,
                        new BigDecimal("30.0000"),
                        "Move capacity"
                )
        );

        ArgumentCaptor<CapitalHistory> historyCaptor = ArgumentCaptor.forClass(CapitalHistory.class);
        verify(capitalHistoryRepository, org.mockito.Mockito.times(2)).saveAndFlush(historyCaptor.capture());
        assertThat(source.getAllocatedAmount()).isEqualByComparingTo("70.0000");
        assertThat(destination.getAllocatedAmount()).isEqualByComparingTo("50.0000");
        assertThat(historyCaptor.getAllValues())
                .extracting(CapitalHistory::getReferenceId)
                .containsExactly(SOURCE_TASK_ID, DESTINATION_TASK_ID);
        assertThat(historyCaptor.getAllValues())
                .extracting(CapitalHistory::getActionType)
                .containsExactly(CapitalActionType.REALLOCATE, CapitalActionType.REALLOCATE);
        assertThat(response.targetId()).isEqualTo(DESTINATION_TASK_ID);
        assertThat(response.targetAllocatedAmount()).isEqualByComparingTo("50.0000");
        assertThat(response.totalAllocatedAmount()).isEqualByComparingTo("120.0000");
    }

    @Test
    void reallocateCapitalRejectsWhenSourceHasInsufficientAmount() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 200L);
        CapitalAllocation source = CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID,
                new BigDecimal("20.0000")
        );
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetsForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                List.of(SOURCE_TASK_ID, DESTINATION_TASK_ID).stream().sorted().toList()
        )).thenReturn(List.of(source));

        assertThatThrownBy(() -> createService().reallocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new ReallocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        AllocationTargetType.TASK,
                        DESTINATION_TASK_ID,
                        new BigDecimal("30.0000"),
                        "Too much"
                )
        )).isInstanceOf(InsufficientAllocatedCapitalException.class);

        verify(capitalHistoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void releaseCapitalMarksStateRowReleasedWhenAllocationIsFullyReleased() {
        CapitalCycle cycle = draftCycle();
        TimeCapital timeCapital = TimeCapital.create(cycle, 100L);
        CapitalAllocation allocation = CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID,
                new BigDecimal("40.0000")
        );
        whenOwnedCycle(cycle);
        when(timeCapitalRepository.findByCapitalCycleIdForUpdate(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(capitalAllocationRepository.findTargetForUpdate(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).thenReturn(Optional.of(allocation));
        when(capitalAllocationRepository.sumAllocatedAmount(OWNER_ID, CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("40.0000"));
        stubHistoryIds();

        AllocationResponse response = createService().releaseCapital(
                OWNER_ID,
                CYCLE_ID,
                new ReleaseCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("40.0000"),
                        "Done"
                )
        );

        verify(capitalAllocationRepository, never()).delete(allocation);
        ArgumentCaptor<CapitalHistory> historyCaptor = ArgumentCaptor.forClass(CapitalHistory.class);
        verify(capitalHistoryRepository).saveAndFlush(historyCaptor.capture());
        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.RELEASED);
        assertThat(historyCaptor.getValue().getActionType()).isEqualTo(CapitalActionType.RELEASE);
        assertThat(historyCaptor.getValue().getBeforeAmount()).isEqualByComparingTo("40.0000");
        assertThat(historyCaptor.getValue().getAfterAmount()).isEqualByComparingTo("0.0000");
        assertThat(response.targetAllocatedAmount()).isEqualByComparingTo("0.0000");
        assertThat(response.totalAllocatedAmount()).isEqualByComparingTo("0.0000");
    }

    @Test
    void timeAllocationRejectsFractionalMinutes() {
        assertThatThrownBy(() -> createService().allocateCapital(
                OWNER_ID,
                CYCLE_ID,
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("1.5000"),
                        false,
                        null
                )
        )).isInstanceOf(InvalidAllocationAmountException.class);

        verifyNoInteractions(capitalCycleRepository, timeCapitalRepository, capitalAllocationRepository);
    }

    private AllocationServiceImpl createService() {
        return new AllocationServiceImpl(
                capitalCycleRepository,
                timeCapitalRepository,
                moneyCapitalRepository,
                capitalAllocationRepository,
                capitalHistoryRepository,
                allocationTargetValidator,
                new DefaultAllocationValidator()
        );
    }

    private void whenOwnedCycle(CapitalCycle cycle) {
        if (cycle.getStatus() != CapitalCycleStatus.ACTIVE) {
            cycle.activate(Instant.parse("2026-08-04T00:00:00Z"));
        }
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
    }

    private void stubHistoryIds() {
        AtomicInteger counter = new AtomicInteger(1);
        when(capitalHistoryRepository.saveAndFlush(any(CapitalHistory.class))).thenAnswer(invocation -> {
            CapitalHistory history = invocation.getArgument(0);
            setField(
                    history,
                    "id",
                    UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa" + counter.getAndIncrement())
            );
            return history;
        });
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
