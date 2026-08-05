package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyCapitalAdjustmentResponse(
        UUID capitalCycleId,
        CapitalActionType actionType,
        BigDecimal amount,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        String currencyCode,
        String reason,
        UUID historyId
) {
}
