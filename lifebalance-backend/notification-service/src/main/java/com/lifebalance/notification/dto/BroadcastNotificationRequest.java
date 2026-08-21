package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record BroadcastNotificationRequest(
        @NotEmpty Set<@NotNull UUID> recipientIds,
        @NotNull NotificationEventType eventType,
        Set<@NotNull NotificationChannel> channels,
        NotificationPriority priority,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String message,
        @Size(max = 64) String referenceType,
        UUID referenceId,
        @NotBlank @Size(max = 500) String purpose,
        Boolean policyApproved,
        OffsetDateTime scheduledAt,
        @Size(max = 1000) String reason
) {
}
