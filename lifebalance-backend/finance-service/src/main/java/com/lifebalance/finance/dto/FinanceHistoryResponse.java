package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FinanceHistoryResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        FinanceHistoryActionType actionType,
        FinanceReferenceType referenceType,
        UUID referenceId,
        String reason,
        String oldValue,
        String newValue,
        OffsetDateTime occurredAt
) {
}
