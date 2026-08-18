package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdjustMoneyCapitalRequest(
        @NotNull
        CapitalAdjustmentType adjustmentType,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @NotBlank
        @Size(max = 1000)
        String reason,

        boolean allowOverAllocation,

        @Size(max = 128)
        String overAllocationConfirmationKey
) {

    public AdjustMoneyCapitalRequest(
            CapitalAdjustmentType adjustmentType,
            BigDecimal amount,
            String reason,
            boolean allowOverAllocation
    ) {
        this(adjustmentType, amount, reason, allowOverAllocation, null);
    }
}
