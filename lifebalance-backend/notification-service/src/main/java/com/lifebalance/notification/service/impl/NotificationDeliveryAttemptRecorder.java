package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationDeliveryAttempt;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.repository.NotificationDeliveryAttemptRepository;
import org.springframework.stereotype.Component;

@Component
class NotificationDeliveryAttemptRecorder {

    private final NotificationDeliveryAttemptRepository attemptRepository;

    NotificationDeliveryAttemptRecorder(NotificationDeliveryAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    void record(
            NotificationRecord notification,
            NotificationDeliveryStatus deliveryStatus,
            String providerMessageId,
            String errorMessage
    ) {
        int attemptNumber = Math.toIntExact(attemptRepository.countByNotificationId(notification.getId()) + 1);
        attemptRepository.save(NotificationDeliveryAttempt.record(
                notification,
                attemptNumber,
                deliveryStatus,
                providerMessageId,
                errorMessage
        ));
    }
}
