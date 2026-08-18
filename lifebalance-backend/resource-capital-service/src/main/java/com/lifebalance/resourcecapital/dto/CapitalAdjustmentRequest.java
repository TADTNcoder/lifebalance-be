package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CapitalAdjustmentRequest(
        @NotNull
        UUID capitalCycleId,

        @NotNull
        CapitalKind capitalType,

        @NotNull
        CapitalAdjustmentType adjustmentType,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @NotBlank
        @Size(max = 1000)
        String reason,

        boolean allowOverAllocation,

        @Size(max = 128)
        String overAllocationConfirmationKey
) {

    public CapitalAdjustmentRequest(
            UUID capitalCycleId,
            CapitalKind capitalType,
            CapitalAdjustmentType adjustmentType,
            BigDecimal amount,
            String reason,
            boolean allowOverAllocation
    ) {
        this(capitalCycleId, capitalType, adjustmentType, amount, reason, allowOverAllocation, null);
    }
}
