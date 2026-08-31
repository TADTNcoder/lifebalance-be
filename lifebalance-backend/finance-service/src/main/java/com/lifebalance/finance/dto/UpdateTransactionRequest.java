package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceIncomeSourceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateTransactionRequest(
        @NotNull FinanceTransactionType transactionType,
        UUID sourceAccountId,
        UUID destinationAccountId,
        UUID categoryId,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currencyCode,
        @NotNull OffsetDateTime transactionDate,
        @Size(max = 255) String transactionName,
        @Size(max = 1000) String description,
        UUID taskId,
        UUID capitalCycleId,
        UUID capitalAllocationId,
        FinanceIncomeSourceType incomeSourceType,
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$") String salaryPeriod,
        @Positive @Digits(integer = 15, fraction = 4) BigDecimal baseSalary,
        @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal bonusAmount,
        @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal deductionAmount,
        @NotBlank @Size(max = 1000) String reason
) {

    public UpdateTransactionRequest(
            FinanceTransactionType transactionType,
            UUID sourceAccountId,
            UUID destinationAccountId,
            UUID categoryId,
            BigDecimal amount,
            String currencyCode,
            OffsetDateTime transactionDate,
            String transactionName,
            String description,
            UUID taskId,
            UUID capitalCycleId,
            UUID capitalAllocationId,
            String reason
    ) {
        this(
                transactionType,
                sourceAccountId,
                destinationAccountId,
                categoryId,
                amount,
                currencyCode,
                transactionDate,
                transactionName,
                description,
                taskId,
                capitalCycleId,
                capitalAllocationId,
                FinanceIncomeSourceType.ONE_OFF,
                null,
                null,
                null,
                null,
                reason
        );
    }

    public UpdateTransactionRequest(
            FinanceTransactionType transactionType,
            UUID sourceAccountId,
            UUID destinationAccountId,
            UUID categoryId,
            BigDecimal amount,
            String currencyCode,
            OffsetDateTime transactionDate,
            String description,
            UUID taskId,
            UUID capitalCycleId,
            UUID capitalAllocationId,
            String reason
    ) {
        this(
                transactionType,
                sourceAccountId,
                destinationAccountId,
                categoryId,
                amount,
                currencyCode,
                transactionDate,
                null,
                description,
                taskId,
                capitalCycleId,
                capitalAllocationId,
                reason
        );
    }
}
