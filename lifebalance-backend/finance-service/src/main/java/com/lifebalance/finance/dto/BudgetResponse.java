package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.BudgetStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID ownerId,
        UUID categoryId,
        String categoryName,
        String name,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal amountLimit,
        String currencyCode,
        BigDecimal alertThresholdPercent,
        BudgetStatus status,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
