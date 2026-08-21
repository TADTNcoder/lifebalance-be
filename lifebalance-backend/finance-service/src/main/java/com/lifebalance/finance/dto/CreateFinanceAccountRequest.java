package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceAccountType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateFinanceAccountRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull FinanceAccountType accountType,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currencyCode,
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal openingBalance
) {
}
