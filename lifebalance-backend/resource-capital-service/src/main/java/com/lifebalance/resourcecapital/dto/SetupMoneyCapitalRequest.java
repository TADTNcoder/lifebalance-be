package com.lifebalance.resourcecapital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SetupMoneyCapitalRequest(
        @NotNull
        @PositiveOrZero
        BigDecimal plannedAmount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currencyCode
) {
}
