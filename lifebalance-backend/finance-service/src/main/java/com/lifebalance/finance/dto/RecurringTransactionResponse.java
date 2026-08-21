package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.RecurrenceFrequency;
import com.lifebalance.finance.domain.RecurringTransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID ownerId,
        FinanceTransactionType transactionType,
        RecurringTransactionStatus status,
        UUID sourceAccountId,
        String sourceAccountName,
        UUID destinationAccountId,
        String destinationAccountName,
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        String currencyCode,
        RecurrenceFrequency frequency,
        int intervalCount,
        LocalDate startsOn,
        LocalDate nextRunDate,
        LocalDate endsOn,
        String description,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
