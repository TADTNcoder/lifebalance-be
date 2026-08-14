package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CapitalAllocationResponse(
        UUID id,
        UUID capitalCycleId,
        CapitalKind capitalType,
        AllocationTargetType targetType,
        UUID targetId,
        UUID taskId,
        BigDecimal allocatedAmount,
        BigDecimal spentAmount,
        AllocationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
