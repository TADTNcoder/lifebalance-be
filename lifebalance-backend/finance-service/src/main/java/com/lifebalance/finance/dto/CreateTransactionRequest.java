package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceTransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull FinanceTransactionType transactionType,
        UUID sourceAccountId,
        UUID destinationAccountId,
        UUID categoryId,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currencyCode,
        @NotNull OffsetDateTime transactionDate,
        @Size(max = 1000) String description,
        UUID taskId,
        UUID capitalCycleId,
        UUID capitalAllocationId,
        @Size(max = 1000) String reason
) {
}
