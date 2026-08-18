package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocateCapitalRequestDTO(
        @NotNull
        UUID capitalCycleId,

        @NotNull
        CapitalKind capitalType,

        @NotNull
        AllocationTargetType targetType,

        UUID targetId,

        UUID taskId,

        UUID taskCatalogId,

        UUID projectId,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        boolean overAllocationConfirmed,

        @Size(max = 128)
        String overAllocationConfirmationKey,

        @Size(max = 1000)
        String reason
) {

    public AllocateCapitalRequestDTO(
            UUID capitalCycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            UUID taskId,
            UUID taskCatalogId,
            UUID projectId,
            BigDecimal amount,
            boolean overAllocationConfirmed,
            String reason
    ) {
        this(
                capitalCycleId,
                capitalType,
                targetType,
                targetId,
                taskId,
                taskCatalogId,
                projectId,
                amount,
                overAllocationConfirmed,
                null,
                reason
        );
    }
}
