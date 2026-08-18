package com.lifebalance.resourcecapital.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CapitalAllocationChangeRequest(
        @NotNull
        @PositiveOrZero
        @Digits(integer = 15, fraction = 4)
        BigDecimal newAmount,

        boolean overAllocationConfirmed,

        @Size(max = 128)
        String overAllocationConfirmationKey,

        @Size(max = 1000)
        String reason
) {

    public CapitalAllocationChangeRequest(
            BigDecimal newAmount,
            boolean overAllocationConfirmed,
            String reason
    ) {
        this(newAmount, overAllocationConfirmed, null, reason);
    }
}
