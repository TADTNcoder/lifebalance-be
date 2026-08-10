package com.lifebalance.resourcecapital.dto;

import java.math.BigDecimal;

public record RemainingMoneyCapitalResponse(
        BigDecimal plannedAmount,
        BigDecimal allocatedAmount,
        BigDecimal remainingAmount,
        String currencyCode,
        boolean overAllocated,
        boolean initialized
) {
}
