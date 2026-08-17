package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AllocationResponseDTO(
        UUID allocationId,
        UUID capitalCycleId,
        CapitalKind capitalType,
        AllocationTargetType targetType,
        UUID targetId,
        BigDecimal targetAllocatedAmount,
        BigDecimal spentAmount,
        BigDecimal releasedAmount,
        AllocationStatus status,
        BigDecimal plannedAmount,
        BigDecimal totalAllocatedAmount,
        BigDecimal remainingAmount,
        boolean overAllocated,
        boolean overAllocationConfirmed,
        List<UUID> historyIds,
        Instant createdAt,
        Instant updatedAt
) {

    public static AllocationResponseDTO from(AllocationResponse response) {
        if (response == null) {
            return null;
        }
        return new AllocationResponseDTO(
                null,
                response.cycleId(),
                response.capitalType(),
                response.targetType(),
                response.targetId(),
                response.targetAllocatedAmount(),
                null,
                null,
                null,
                response.plannedAmount(),
                response.totalAllocatedAmount(),
                response.remainingAmount(),
                response.overAllocated(),
                response.overAllocated(),
                response.historyIds(),
                null,
                null
        );
    }
}
