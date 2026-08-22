package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;

import java.math.BigDecimal;
import java.util.UUID;

public record OverAllocationConfirmationResponse(
        boolean confirmationRequired,
        String confirmationField,
        String confirmationKey,
        String operationType,
        String operationReference,
        UUID capitalCycleId,
        CapitalKind capitalType,
        BigDecimal availableAmount,
        BigDecimal requestedAmount,
        BigDecimal shortageAmount,
        BigDecimal projectedRemainingAmount,
        String remainingState,
        String remainingExplanation
) {
}
