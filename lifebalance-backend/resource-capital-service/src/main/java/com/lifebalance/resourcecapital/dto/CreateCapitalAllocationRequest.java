package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCapitalAllocationRequest(
        @NotNull
        UUID capitalCycleId,

        @NotNull
        CapitalKind capitalType,

        @NotNull
        AllocationTargetType targetType,

        UUID targetId,

        UUID taskId,

        UUID taskCatalogId,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        boolean allowOverAllocation,

        @Size(max = 128)
        String overAllocationConfirmationKey,

        @Size(max = 1000)
        String reason
) {

    public CreateCapitalAllocationRequest(
            UUID capitalCycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            UUID taskId,
            UUID taskCatalogId,
            BigDecimal amount,
            boolean allowOverAllocation,
            String reason
    ) {
        this(
                capitalCycleId,
                capitalType,
                targetType,
                targetId,
                taskId,
                taskCatalogId,
                amount,
                allowOverAllocation,
                null,
                reason
        );
    }
}
