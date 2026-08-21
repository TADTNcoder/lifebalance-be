package com.lifebalance.notification.service;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationStatus;
import com.lifebalance.notification.dto.BroadcastNotificationRequest;
import com.lifebalance.notification.dto.BulkNotificationActionResponse;
import com.lifebalance.notification.dto.CreateNotificationRequest;
import com.lifebalance.notification.dto.NotificationReasonRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.UnreadCountResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    List<NotificationResponse> create(UUID ownerId, CreateNotificationRequest request);

    List<NotificationResponse> broadcast(UUID actorId, BroadcastNotificationRequest request);

    NotificationResponse getById(UUID ownerId, UUID notificationId);

    Page<NotificationResponse> search(
            UUID ownerId,
            NotificationStatus status,
            NotificationEventType eventType,
            NotificationChannel channel,
            NotificationDeliveryStatus deliveryStatus,
            String referenceType,
            UUID referenceId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    NotificationResponse markRead(UUID ownerId, UUID notificationId);

    NotificationResponse markUnread(UUID ownerId, UUID notificationId);

    BulkNotificationActionResponse markAllRead(UUID ownerId);

    NotificationResponse archive(UUID ownerId, UUID notificationId, NotificationReasonRequest request);

    UnreadCountResponse unreadCount(UUID ownerId);
}
