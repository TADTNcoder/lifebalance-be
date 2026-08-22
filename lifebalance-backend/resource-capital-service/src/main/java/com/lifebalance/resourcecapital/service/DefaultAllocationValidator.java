package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.OverAllocationConfirmation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationNotAllowedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Component
public class DefaultAllocationValidator {

    private static final int MONEY_SCALE = 4;

    public AllocationValidationResult validateNewAllocation(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal totalCapital,
            BigDecimal currentActiveAllocations,
            BigDecimal spentCapital,
            BigDecimal requestedAmount,
            boolean allowOverAllocation
    ) {
        return validateNewAllocation(
                cycle,
                capitalType,
                totalCapital,
                currentActiveAllocations,
                spentCapital,
                requestedAmount,
                allowOverAllocation,
                null,
                "ALLOCATE",
                "ALLOCATION_TARGET:UNKNOWN"
        );
    }

    public AllocationValidationResult validateNewAllocation(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal totalCapital,
            BigDecimal currentActiveAllocations,
            BigDecimal spentCapital,
            BigDecimal requestedAmount,
            boolean allowOverAllocation,
            String overAllocationConfirmationKey,
            String operationType,
            String operationReference
    ) {
        Objects.requireNonNull(cycle, "Capital cycle is required.");
        Objects.requireNonNull(capitalType, "Capital type is required.");

        AllocationValidationSnapshot snapshot = inspectNewAllocation(
                cycle,
                capitalType,
                totalCapital,
                currentActiveAllocations,
                spentCapital,
                requestedAmount
        );

        if (!snapshot.overAllocated()) {
            return new AllocationValidationResult(
                    snapshot.availableCapital(),
                    snapshot.remainingAfterAllocation(),
                    false
            );
        }

        if (!cycle.isOverAllocationAllowed()) {
            throw new OverAllocationNotAllowedException(
                    cycle.getId(),
                    capitalType,
                    snapshot.availableCapital(),
                    snapshot.requestedAmount(),
                    snapshot.remainingAfterAllocation()
            );
        }
        String expectedConfirmationKey = OverAllocationConfirmation.confirmationKey(
                operationType,
                cycle.getId(),
                capitalType,
                operationReference,
                snapshot.requestedAmount(),
                snapshot.availableCapital(),
                snapshot.remainingAfterAllocation()
        );
        if (!allowOverAllocation || !OverAllocationConfirmation.matches(
                overAllocationConfirmationKey,
                expectedConfirmationKey
        )) {
            throw new OverAllocationConfirmationRequiredException(
                    cycle.getId(),
                    capitalType,
                    snapshot.availableCapital(),
                    snapshot.requestedAmount(),
                    snapshot.remainingAfterAllocation(),
                    operationType,
                    operationReference
            );
        }

        return new AllocationValidationResult(snapshot.availableCapital(), snapshot.remainingAfterAllocation(), true);
    }

    public AllocationValidationSnapshot inspectNewAllocation(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal totalCapital,
            BigDecimal currentActiveAllocations,
            BigDecimal spentCapital,
            BigDecimal requestedAmount
    ) {
        Objects.requireNonNull(cycle, "Capital cycle is required.");
        Objects.requireNonNull(capitalType, "Capital type is required.");

        BigDecimal normalizedTotalCapital = normalize(totalCapital);
        BigDecimal normalizedActiveAllocations = normalize(currentActiveAllocations);
        BigDecimal normalizedSpentCapital = normalize(spentCapital);
        BigDecimal normalizedRequestedAmount = normalize(requestedAmount);
        BigDecimal availableCapital = normalizedTotalCapital
                .subtract(normalizedActiveAllocations)
                .subtract(normalizedSpentCapital)
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        BigDecimal remainingAfterAllocation = availableCapital.subtract(normalizedRequestedAmount)
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);

        return new AllocationValidationSnapshot(
                availableCapital,
                remainingAfterAllocation,
                normalizedRequestedAmount,
                remainingAfterAllocation.compareTo(BigDecimal.ZERO) < 0,
                cycle.isOverAllocationAllowed()
        );
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    public record AllocationValidationResult(
            BigDecimal availableCapital,
            BigDecimal remainingAfterAllocation,
            boolean overAllocated
    ) {
    }

    public record AllocationValidationSnapshot(
            BigDecimal availableCapital,
            BigDecimal remainingAfterAllocation,
            BigDecimal requestedAmount,
            boolean overAllocated,
            boolean overAllocationAllowed
    ) {
    }
}
