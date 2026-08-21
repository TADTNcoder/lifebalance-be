package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationTemplateResponse(
        UUID id,
        UUID ownerId,
        String templateKey,
        NotificationEventType eventType,
        NotificationChannel channel,
        String titleTemplate,
        String messageTemplate,
        boolean enabled,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
