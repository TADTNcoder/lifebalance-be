package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AllocationResponse(
        UUID cycleId,
        CapitalKind capitalType,
        AllocationTargetType targetType,
        UUID targetId,
        BigDecimal targetAllocatedAmount,
        BigDecimal plannedAmount,
        BigDecimal totalAllocatedAmount,
        BigDecimal remainingAmount,
        boolean overAllocated,
        List<UUID> historyIds
) {
}
