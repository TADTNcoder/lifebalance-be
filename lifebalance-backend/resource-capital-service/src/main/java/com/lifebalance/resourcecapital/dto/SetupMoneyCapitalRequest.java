package com.lifebalance.resourcecapital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SetupMoneyCapitalRequest(
        @NotNull
        @PositiveOrZero
        @Digits(integer = 15, fraction = 4)
        BigDecimal plannedAmount,

        @NotBlank
        @Size(min = 3, max = 3)
        @Pattern(regexp = "[A-Za-z]{3}", message = "currencyCode must contain exactly three letters")
        String currencyCode
) {
}
