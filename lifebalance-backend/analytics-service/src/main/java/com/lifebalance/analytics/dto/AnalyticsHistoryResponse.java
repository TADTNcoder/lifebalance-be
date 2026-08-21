package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AnalyticsHistoryResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        AnalyticsHistoryActionType actionType,
        UUID actualRecordId,
        UUID evaluationResultId,
        UUID reportId,
        String oldValue,
        String newValue,
        String reason,
        OffsetDateTime occurredAt
) {
}
