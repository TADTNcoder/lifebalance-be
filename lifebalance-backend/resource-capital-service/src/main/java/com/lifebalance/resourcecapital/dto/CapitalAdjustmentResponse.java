package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CapitalAdjustmentResponse(
        Long id,
        UUID capitalCycleId,
        CapitalKind capitalType,
        CapitalAdjustmentType adjustmentType,
        CapitalActionType historyActionType,
        BigDecimal amount,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        String reason,
        UUID historyId,
        LocalDateTime createdAt
) {
}
