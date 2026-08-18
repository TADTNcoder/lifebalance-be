package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.OverAllocationConfirmation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
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
    void requiresConfirmationWhenAllocationExceedsAvailableCapitalAndPolicyAllowsIt() {
        CapitalCycle cycle = activeCycle(true);

        assertThatThrownBy(() -> validator.validateNewAllocation(
                cycle,
                CapitalKind.TIME,
                new BigDecimal("100.0000"),
                new BigDecimal("80.0000"),
                new BigDecimal("10.0000"),
                new BigDecimal("15.0000"),
                false
        )).isInstanceOf(OverAllocationConfirmationRequiredException.class)
                .satisfies(exception -> {
                    OverAllocationConfirmationRequiredException capitalException =
                            (OverAllocationConfirmationRequiredException) exception;
                    assertThat(capitalException.getCode())
                            .isEqualTo(OverAllocationConfirmationRequiredException.ERROR_CODE);
                    assertThat(capitalException.getMessage()).contains("TIME");
                    assertThat(capitalException.getMessage()).contains("Available amount: 10.0000");
                    assertThat(capitalException.getMessage()).contains("requested amount: 15.0000");
                    assertThat(capitalException.getMessage()).contains("projected remaining amount: -5.0000");
                    assertThat(capitalException.getDetails())
                            .containsEntry("confirmationRequired", "true")
                            .containsEntry("confirmationField", "overAllocationConfirmationKey")
                            .containsEntry("availableAmount", "10.0000")
                            .containsEntry("requestedAmount", "15.0000")
                            .containsEntry("shortageAmount", "5.0000")
                            .containsEntry("projectedRemainingAmount", "-5.0000")
                            .containsKey("confirmationKey");
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
        String confirmationKey = OverAllocationConfirmation.confirmationKey(
                "ALLOCATE",
                cycle.getId(),
                CapitalKind.TIME,
                "ALLOCATION_TARGET:UNKNOWN",
                new BigDecimal("10.0000"),
                new BigDecimal("5.0000"),
                new BigDecimal("-5.0000")
        );

        DefaultAllocationValidator.AllocationValidationResult result = validator.validateNewAllocation(
                cycle,
                CapitalKind.TIME,
                new BigDecimal("100.0000"),
                new BigDecimal("95.0000"),
                BigDecimal.ZERO,
                new BigDecimal("10.0000"),
                true,
                confirmationKey,
                "ALLOCATE",
                "ALLOCATION_TARGET:UNKNOWN"
        );

        assertThat(result.availableCapital()).isEqualByComparingTo("5.0000");
        assertThat(result.remainingAfterAllocation()).isEqualByComparingTo("-5.0000");
        assertThat(result.overAllocated()).isTrue();
    }

    @Test
    void rejectsConfirmedOverAllocationWhenConfirmationKeyDoesNotMatchOperationSnapshot() {
        CapitalCycle cycle = activeCycle(true);

        assertThatThrownBy(() -> validator.validateNewAllocation(
                cycle,
                CapitalKind.TIME,
                new BigDecimal("100.0000"),
                new BigDecimal("95.0000"),
                BigDecimal.ZERO,
                new BigDecimal("10.0000"),
                true,
                "oac_wrong_key",
                "ALLOCATE",
                "ALLOCATION_TARGET:UNKNOWN"
        )).isInstanceOf(OverAllocationConfirmationRequiredException.class);
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
