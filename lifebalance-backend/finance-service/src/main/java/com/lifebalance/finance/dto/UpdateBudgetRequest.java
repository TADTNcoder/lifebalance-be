package com.lifebalance.finance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateBudgetRequest(
        UUID categoryId,
        @NotBlank @Size(max = 120) String name,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amountLimit,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currencyCode,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
        BigDecimal alertThresholdPercent,
        @NotBlank @Size(max = 1000) String reason
) {
}
