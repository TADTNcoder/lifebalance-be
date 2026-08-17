package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAvailableCapitalException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationNotAllowedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultAllocationValidatorTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final DefaultAllocationValidator validator = new DefaultAllocationValidator();

    @Test
    void rejectsAllocationAboveAvailableCapitalWithoutOverAllocationConfirmation() {
        CapitalCycle cycle = activeCycle(false);

        assertThatThrownBy(() -> validator.validateNewAllocation(
                cycle,
                CapitalKind.TIME,
                new BigDecimal("100.0000"),
                new BigDecimal("80.0000"),
                new BigDecimal("10.0000"),
                new BigDecimal("15.0000"),
                false
        )).isInstanceOf(InsufficientAvailableCapitalException.class)
                .satisfies(exception -> {
                    InsufficientAvailableCapitalException capitalException =
                            (InsufficientAvailableCapitalException) exception;
                    assertThat(capitalException.getCode()).isEqualTo("INSUFFICIENT_AVAILABLE_CAPITAL");
                    assertThat(capitalException.getMessage()).contains("TIME");
                    assertThat(capitalException.getMessage()).contains("Available amount: 10.0000");
                    assertThat(capitalException.getMessage()).contains("requested amount: 15.0000");
                });
    }

    @Test
    void rejectsConfirmedOverAllocationWhenCyclePolicyDisallowsIt() {
        CapitalCycle cycle = activeCycle(false);

        assertThatThrownBy(() -> validator.validateNewAllocation(
                cycle,
                CapitalKind.MONEY,
                new BigDecimal("100.0000"),
                new BigDecimal("90.0000"),
                BigDecimal.ZERO,
                new BigDecimal("20.0000"),
                true
        )).isInstanceOf(OverAllocationNotAllowedException.class);
    }

    @Test
    void returnsProjectedRemainingWhenAllocationFitsAvailableCapital() {
        CapitalCycle cycle = activeCycle(false);

        DefaultAllocationValidator.AllocationValidationResult result = validator.validateNewAllocation(
                cycle,
                CapitalKind.MONEY,
                new BigDecimal("100.0000"),
                new BigDecimal("60.0000"),
                new BigDecimal("10.0000"),
                new BigDecimal("25.0000"),
                false
        );

        assertThat(result.availableCapital()).isEqualByComparingTo("30.0000");
        assertThat(result.remainingAfterAllocation()).isEqualByComparingTo("5.0000");
        assertThat(result.overAllocated()).isFalse();
    }

    @Test
    void allowsConfirmedOverAllocationWhenCyclePolicyAllowsIt() {
        CapitalCycle cycle = activeCycle(true);

        DefaultAllocationValidator.AllocationValidationResult result = validator.validateNewAllocation(
                cycle,
                CapitalKind.TIME,
                new BigDecimal("100.0000"),
                new BigDecimal("95.0000"),
                BigDecimal.ZERO,
                new BigDecimal("10.0000"),
                true
        );

        assertThat(result.availableCapital()).isEqualByComparingTo("5.0000");
        assertThat(result.remainingAfterAllocation()).isEqualByComparingTo("-5.0000");
        assertThat(result.overAllocated()).isTrue();
    }

    private CapitalCycle activeCycle(boolean overAllocationAllowed) {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                "Daily resource cycle",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 17)
        );
        if (overAllocationAllowed) {
            cycle.allowOverAllocation();
        }
        cycle.activate(Instant.parse("2026-08-17T00:00:00Z"));
        return cycle;
    }
}
