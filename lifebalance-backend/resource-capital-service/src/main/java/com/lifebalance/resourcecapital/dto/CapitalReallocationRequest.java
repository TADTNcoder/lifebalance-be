package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CapitalReallocationRequest(
        @NotNull
        UUID capitalCycleId,

        @NotNull
        CapitalKind capitalType,

        UUID sourceAllocationId,

        AllocationTargetType sourceTargetType,

        UUID sourceTargetId,

        UUID sourceTaskId,

        UUID destinationAllocationId,

        AllocationTargetType destinationTargetType,

        UUID destinationTargetId,

        UUID destinationTaskId,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @Size(max = 1000)
        String reason
) {
}
