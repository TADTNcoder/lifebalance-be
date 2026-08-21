package com.lifebalance.notification.service;

import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.dto.NotificationHistoryResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationHistoryService {

    Page<NotificationHistoryResponse> getHistory(
            UUID ownerId,
            UUID notificationId,
            NotificationHistoryActionType actionType,
            Pageable pageable
    );
}
