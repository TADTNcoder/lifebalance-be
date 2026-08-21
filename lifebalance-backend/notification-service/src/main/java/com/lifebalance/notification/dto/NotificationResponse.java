package com.lifebalance.notification.dto;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationPriority;
import com.lifebalance.notification.domain.NotificationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID ownerId,
        UUID recipientId,
        UUID actorId,
        NotificationEventType eventType,
        NotificationChannel channel,
        NotificationPriority priority,
        NotificationStatus status,
        NotificationDeliveryStatus deliveryStatus,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        String purpose,
        OffsetDateTime scheduledAt,
        OffsetDateTime sentAt,
        OffsetDateTime readAt,
        OffsetDateTime archivedAt,
        OffsetDateTime failedAt,
        String failureReason,
        String providerMessageId,
        int retryCount,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
