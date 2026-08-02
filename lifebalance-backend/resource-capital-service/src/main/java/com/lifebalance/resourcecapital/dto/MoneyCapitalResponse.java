package com.lifebalance.resourcecapital.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyCapitalResponse(
        UUID id,
        UUID cycleId,
        BigDecimal plannedAmount,
        BigDecimal allocatedAmount,
        BigDecimal availableAmount,
        BigDecimal remainingAmount,
        String currencyCode,
        boolean initialized
) {
}
