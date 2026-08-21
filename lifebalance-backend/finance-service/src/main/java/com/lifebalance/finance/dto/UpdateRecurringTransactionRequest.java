package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.RecurrenceFrequency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateRecurringTransactionRequest(
        @NotNull FinanceTransactionType transactionType,
        UUID sourceAccountId,
        UUID destinationAccountId,
        UUID categoryId,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currencyCode,
        @NotNull RecurrenceFrequency frequency,
        @Min(1) int intervalCount,
        @NotNull LocalDate startsOn,
        @NotNull LocalDate nextRunDate,
        LocalDate endsOn,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 1000) String reason
) {
}
