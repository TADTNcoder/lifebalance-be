package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        UUID ownerId,
        NotificationEventType eventType,
        NotificationChannel channel,
        boolean enabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        String timezone,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
