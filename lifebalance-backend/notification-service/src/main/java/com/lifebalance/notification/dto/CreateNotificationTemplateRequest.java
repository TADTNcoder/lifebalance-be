package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationTemplateRequest(
        @NotBlank @Size(max = 120) String templateKey,
        @NotNull NotificationEventType eventType,
        @NotNull NotificationChannel channel,
        @NotBlank @Size(max = 200) String titleTemplate,
        @NotBlank @Size(max = 2000) String messageTemplate,
        Boolean enabled
) {
}
