package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationHistoryActionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationHistoryResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        NotificationHistoryActionType actionType,
        UUID notificationId,
        String oldValue,
        String newValue,
        String reason,
        OffsetDateTime occurredAt
) {
}
