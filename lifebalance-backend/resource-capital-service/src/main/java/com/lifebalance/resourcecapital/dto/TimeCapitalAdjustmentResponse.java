package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;

import java.util.UUID;

public record TimeCapitalAdjustmentResponse(
        UUID capitalCycleId,
        CapitalActionType actionType,
        long amountInMinutes,
        long beforeMinutes,
        long afterMinutes,
        String reason,
        UUID historyId
) {
}
