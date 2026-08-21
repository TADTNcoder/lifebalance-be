package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.domain.FinanceAccountType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FinanceAccountResponse(
        UUID id,
        UUID ownerId,
        String name,
        FinanceAccountType accountType,
        String currencyCode,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        FinanceAccountStatus status,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
