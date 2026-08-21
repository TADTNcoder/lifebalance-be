package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.dto.NotificationHistoryResponse;
import com.lifebalance.notification.repository.NotificationHistoryRepository;
import com.lifebalance.notification.service.NotificationHistoryService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private final NotificationHistoryRepository historyRepository;
    private final NotificationMapper mapper;

    NotificationHistoryServiceImpl(NotificationHistoryRepository historyRepository, NotificationMapper mapper) {
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationHistoryResponse> getHistory(
            UUID ownerId,
            UUID notificationId,
            NotificationHistoryActionType actionType,
            Pageable pageable
    ) {
        return historyRepository.search(ownerId, notificationId, actionType, pageable)
                .map(mapper::toHistoryResponse);
    }
}
