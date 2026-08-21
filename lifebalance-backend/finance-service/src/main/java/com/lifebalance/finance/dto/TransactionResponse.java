package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceTransactionStatus;
import com.lifebalance.finance.domain.FinanceTransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID ownerId,
        FinanceTransactionType transactionType,
        FinanceTransactionStatus status,
        UUID sourceAccountId,
        String sourceAccountName,
        UUID destinationAccountId,
        String destinationAccountName,
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        String currencyCode,
        OffsetDateTime transactionDate,
        String description,
        UUID taskId,
        UUID capitalCycleId,
        UUID capitalAllocationId,
        OffsetDateTime voidedAt,
        String voidReason,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
