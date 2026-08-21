package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationHistory;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.repository.NotificationHistoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class NotificationHistoryRecorder {

    private final NotificationHistoryRepository historyRepository;

    NotificationHistoryRecorder(NotificationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    void record(
            UUID ownerId,
            UUID actorId,
            NotificationHistoryActionType actionType,
            NotificationRecord notification,
            String oldValue,
            String newValue,
            String reason
    ) {
        historyRepository.save(NotificationHistory.record(
                ownerId,
                actorId,
                actionType,
                notification,
                oldValue,
                newValue,
                reason
        ));
    }
}
