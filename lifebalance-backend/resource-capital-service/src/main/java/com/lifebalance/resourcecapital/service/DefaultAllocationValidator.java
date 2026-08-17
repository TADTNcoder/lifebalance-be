package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAvailableCapitalException;
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

        if (normalizedRequestedAmount.compareTo(availableCapital) <= 0) {
            return new AllocationValidationResult(availableCapital, remainingAfterAllocation, false);
        }

        if (!allowOverAllocation) {
            throw new InsufficientAvailableCapitalException(
                    cycle.getId(),
                    capitalType,
                    availableCapital,
                    normalizedRequestedAmount
            );
        }
        if (!cycle.isOverAllocationAllowed()) {
            BigDecimal projectedAllocatedAmount = normalizedActiveAllocations.add(normalizedRequestedAmount)
                    .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
            throw new OverAllocationNotAllowedException(
                    cycle.getId(),
                    capitalType,
                    normalizedTotalCapital,
                    projectedAllocatedAmount
            );
        }

        return new AllocationValidationResult(availableCapital, remainingAfterAllocation, true);
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
}
