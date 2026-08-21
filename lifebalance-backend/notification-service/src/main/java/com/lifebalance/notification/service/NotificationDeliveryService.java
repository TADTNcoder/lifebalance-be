package com.lifebalance.notification.service;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.dto.MarkDeliveryFailedRequest;
import com.lifebalance.notification.dto.MarkDeliverySentRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.RetryDeliveryRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationDeliveryService {

    Page<NotificationResponse> getPending(
            UUID ownerId,
            NotificationChannel channel,
            OffsetDateTime dueAt,
            Pageable pageable
    );

    NotificationResponse markSent(UUID ownerId, UUID notificationId, MarkDeliverySentRequest request);

    NotificationResponse markFailed(UUID ownerId, UUID notificationId, MarkDeliveryFailedRequest request);

    NotificationResponse retry(UUID ownerId, UUID notificationId, RetryDeliveryRequest request);
}
