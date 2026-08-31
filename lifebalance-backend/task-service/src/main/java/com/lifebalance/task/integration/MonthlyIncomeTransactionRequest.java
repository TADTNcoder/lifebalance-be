package com.lifebalance.task.integration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Payload sent to finance-service when a complete monthly task group earns
 * its salary. Keep this contract aligned with CreateTransactionRequest while
 * avoiding a compile-time dependency between the two services.
 */
record MonthlyIncomeTransactionRequest(
        String transactionType,
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
        String incomeSourceType,
        String salaryPeriod,
        BigDecimal baseSalary,
        BigDecimal bonusAmount,
        BigDecimal deductionAmount,
        String reason
) {
}
