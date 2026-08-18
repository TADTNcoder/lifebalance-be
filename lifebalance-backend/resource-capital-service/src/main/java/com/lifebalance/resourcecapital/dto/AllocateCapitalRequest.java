package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocateCapitalRequest(
        @NotNull
        CapitalKind capitalType,

        @NotNull
        AllocationTargetType targetType,

        @NotNull
        UUID targetId,

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

    public AllocateCapitalRequest(
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            BigDecimal amount,
            boolean allowOverAllocation,
            String reason
    ) {
        this(capitalType, targetType, targetId, amount, allowOverAllocation, null, reason);
    }
}
