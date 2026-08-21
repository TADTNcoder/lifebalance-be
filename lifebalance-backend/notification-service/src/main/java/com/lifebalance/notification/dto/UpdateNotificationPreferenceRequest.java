package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record UpdateNotificationPreferenceRequest(
        @NotNull NotificationEventType eventType,
        @NotNull NotificationChannel channel,
        @NotNull Boolean enabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        @Size(max = 64) String timezone
) {
}
