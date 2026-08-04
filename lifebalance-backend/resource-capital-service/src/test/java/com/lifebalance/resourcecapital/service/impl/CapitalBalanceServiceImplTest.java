package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.ResourceBreakdownDto;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalBalanceServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Mock
    private TimeCapitalRepository timeCapitalRepository;

    @Mock
    private MoneyCapitalRepository moneyCapitalRepository;

    @Mock
    private CapitalAllocationRepository capitalAllocationRepository;

    @Test
    void getCycleBalanceCalculatesCurrentCapitalBalance() {
        CapitalCycle cycle = draftCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(TimeCapital.create(cycle, 480L)));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID))
                .thenReturn(Optional.of(MoneyCapital.create(cycle, new BigDecimal("1000.0000"), "VND")));
        when(capitalAllocationRepository.sumAllocatedAmount(CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("120.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(CYCLE_ID, CapitalKind.MONEY))
                .thenReturn(new BigDecimal("1250.0000"));

        CapitalBalanceResponse response = createService().getCycleBalance(OWNER_ID, CYCLE_ID);

        assertThat(response.cycleId()).isEqualTo(CYCLE_ID);
        assertThat(response.cycleStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
        assertThat(response.timeCapital().total()).isEqualByComparingTo("480.0000");
        assertThat(response.timeCapital().allocated()).isEqualByComparingTo("120.0000");
        assertThat(response.timeCapital().available()).isEqualByComparingTo("360.0000");
        assertThat(response.timeCapital().remaining()).isEqualByComparingTo("360.0000");
        assertThat(response.timeCapital().allocatedPercentage()).isEqualByComparingTo("25.00");
        assertThat(response.timeCapital().overAllocated()).isFalse();
        assertThat(response.moneyCapital().available()).isEqualByComparingTo("-250.0000");
        assertThat(response.moneyCapital().allocatedPercentage()).isEqualByComparingTo("125.00");
        assertThat(response.moneyCapital().overAllocated()).isTrue();
        assertThat(response.moneyCapital().currencyCode()).isEqualTo("VND");
    }

    @Test
    void getCycleBalanceHandlesUninitializedCapitalAndZeroDivision() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.empty());
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.empty());
        when(capitalAllocationRepository.sumAllocatedAmount(CYCLE_ID, CapitalKind.TIME)).thenReturn(BigDecimal.ZERO);
        when(capitalAllocationRepository.sumAllocatedAmount(CYCLE_ID, CapitalKind.MONEY)).thenReturn(BigDecimal.ZERO);

        CapitalBalanceResponse response = createService().getCycleBalance(OWNER_ID, CYCLE_ID);

        assertThat(response.timeCapital().initialized()).isFalse();
        assertThat(response.timeCapital().total()).isEqualByComparingTo("0.0000");
        assertThat(response.timeCapital().allocatedPercentage()).isEqualByComparingTo("0.00");
        assertThat(response.moneyCapital().initialized()).isFalse();
        assertThat(response.moneyCapital().currencyCode()).isNull();
    }

    @Test
    void getAllocationBreakdownByTargetCalculatesBreakdownPercentages() {
        CapitalCycle cycle = draftCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(TimeCapital.create(cycle, 480L)));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID))
                .thenReturn(Optional.of(MoneyCapital.create(cycle, new BigDecimal("1000.0000"), "VND")));
        when(capitalAllocationRepository.sumAllocatedAmount(CYCLE_ID, CapitalKind.TIME))
                .thenReturn(new BigDecimal("240.0000"));
        when(capitalAllocationRepository.sumAllocatedAmount(CYCLE_ID, CapitalKind.MONEY))
                .thenReturn(new BigDecimal("500.0000"));
        when(capitalAllocationRepository.findAllocationBreakdownByTargetType(CYCLE_ID, AllocationTargetType.TASK))
                .thenReturn(List.of(projection(CapitalKind.TIME, TASK_ID, new BigDecimal("120.0000"))));

        List<ResourceBreakdownDto> breakdown = createService().getAllocationBreakdownByTarget(
                OWNER_ID,
                CYCLE_ID,
                AllocationTargetType.TASK
        );

        assertThat(breakdown).hasSize(1);
        assertThat(breakdown.get(0).capitalType()).isEqualTo(CapitalKind.TIME);
        assertThat(breakdown.get(0).targetType()).isEqualTo(AllocationTargetType.TASK);
        assertThat(breakdown.get(0).targetId()).isEqualTo(TASK_ID);
        assertThat(breakdown.get(0).allocatedAmount()).isEqualByComparingTo("120.0000");
        assertThat(breakdown.get(0).percentageOfTotal()).isEqualByComparingTo("25.00");
        assertThat(breakdown.get(0).percentageOfAllocated()).isEqualByComparingTo("50.00");
    }

    @Test
    void ownerCannotReadAnotherOwnersBalance() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OTHER_OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createService().getCycleBalance(OTHER_OWNER_ID, CYCLE_ID))
                .isInstanceOf(CapitalCycleNotFoundException.class);
    }

    private CapitalBalanceServiceImpl createService() {
        return new CapitalBalanceServiceImpl(
                capitalCycleRepository,
                timeCapitalRepository,
                moneyCapitalRepository,
                capitalAllocationRepository
        );
    }

    private CapitalAllocationRepository.TargetAllocationBreakdownProjection projection(
            CapitalKind capitalType,
            UUID targetId,
            BigDecimal allocatedAmount
    ) {
        return new CapitalAllocationRepository.TargetAllocationBreakdownProjection() {
            @Override
            public CapitalKind getCapitalType() {
                return capitalType;
            }

            @Override
            public AllocationTargetType getTargetType() {
                return AllocationTargetType.TASK;
            }

            @Override
            public UUID getTargetId() {
                return targetId;
            }

            @Override
            public BigDecimal getAllocatedAmount() {
                return allocatedAmount;
            }
        };
    }

    private static CapitalCycle draftCycle() {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
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
