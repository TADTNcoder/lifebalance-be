package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceCategoryStatus;
import com.lifebalance.finance.domain.FinanceCategoryType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FinanceCategoryResponse(
        UUID id,
        UUID ownerId,
        String name,
        FinanceCategoryType categoryType,
        String color,
        String icon,
        FinanceCategoryStatus status,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
