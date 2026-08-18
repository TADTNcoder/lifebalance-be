package com.lifebalance.resourcecapital.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

        @Size(min = 3, max = 3)
        @Pattern(regexp = "[A-Za-z]{3}", message = "currencyCode must contain exactly three letters")
        String currencyCode,

        @JsonAlias("overAllocationConfirmed")
        boolean allowOverAllocation
) {

    public AdjustMoneyCapitalRequest(
            CapitalAdjustmentType adjustmentType,
            BigDecimal amount,
            String reason,
            boolean allowOverAllocation
    ) {
        this(adjustmentType, amount, reason, null, allowOverAllocation);
    }
}
