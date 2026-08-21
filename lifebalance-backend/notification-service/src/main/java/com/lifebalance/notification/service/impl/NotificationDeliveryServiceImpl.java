package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.dto.MarkDeliveryFailedRequest;
import com.lifebalance.notification.dto.MarkDeliverySentRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.RetryDeliveryRequest;
import com.lifebalance.notification.error.NotificationExceptions;
import com.lifebalance.notification.repository.NotificationRepository;
import com.lifebalance.notification.service.NotificationDeliveryService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryAttemptRecorder deliveryAttemptRecorder;
    private final NotificationHistoryRecorder historyRecorder;
    private final NotificationMapper mapper;

    NotificationDeliveryServiceImpl(
            NotificationRepository notificationRepository,
            NotificationDeliveryAttemptRecorder deliveryAttemptRecorder,
            NotificationHistoryRecorder historyRecorder,
            NotificationMapper mapper
    ) {
        this.notificationRepository = notificationRepository;
        this.deliveryAttemptRecorder = deliveryAttemptRecorder;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getPending(
            UUID ownerId,
            NotificationChannel channel,
            OffsetDateTime dueAt,
            Pageable pageable
    ) {
        OffsetDateTime normalizedDueAt = dueAt == null ? OffsetDateTime.now() : dueAt;
        return notificationRepository.findPendingForOwner(ownerId, channel, normalizedDueAt, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markSent(UUID ownerId, UUID notificationId, MarkDeliverySentRequest request) {
        NotificationRecord notification = findForUpdate(ownerId, notificationId);
        String oldSnapshot = mapper.notificationSnapshot(notification);
        notification.markSent(ownerId, request == null ? null : request.providerMessageId());
        notification = notificationRepository.save(notification);
        deliveryAttemptRecorder.record(
                notification,
                NotificationDeliveryStatus.SENT,
                notification.getProviderMessageId(),
                null
        );
        recordHistory(NotificationHistoryActionType.NOTIFICATION_SENT, notification, oldSnapshot, null);
        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markFailed(UUID ownerId, UUID notificationId, MarkDeliveryFailedRequest request) {
        NotificationRecord notification = findForUpdate(ownerId, notificationId);
        String oldSnapshot = mapper.notificationSnapshot(notification);
        notification.markFailed(ownerId, request == null ? null : request.errorMessage());
        notification = notificationRepository.save(notification);
        deliveryAttemptRecorder.record(
                notification,
                NotificationDeliveryStatus.FAILED,
                notification.getProviderMessageId(),
                notification.getFailureReason()
        );
        recordHistory(NotificationHistoryActionType.NOTIFICATION_FAILED, notification, oldSnapshot, null);
        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse retry(UUID ownerId, UUID notificationId, RetryDeliveryRequest request) {
        NotificationRecord notification = findForUpdate(ownerId, notificationId);
        String oldSnapshot = mapper.notificationSnapshot(notification);
        String reason = request == null ? null : request.reason();
        notification.retry(ownerId);
        notification = notificationRepository.save(notification);
        deliveryAttemptRecorder.record(notification, NotificationDeliveryStatus.PENDING, null, null);
        recordHistory(NotificationHistoryActionType.NOTIFICATION_RETRIED, notification, oldSnapshot, reason);
        return mapper.toResponse(notification);
    }

    private NotificationRecord findForUpdate(UUID ownerId, UUID notificationId) {
        return notificationRepository.findByIdAndOwnerIdForUpdate(notificationId, ownerId)
                .orElseThrow(() -> NotificationExceptions.notificationNotFound(notificationId));
    }

    private void recordHistory(
            NotificationHistoryActionType actionType,
            NotificationRecord notification,
            String oldSnapshot,
            String reason
    ) {
        historyRecorder.record(
                notification.getOwnerId(),
                notification.getOwnerId(),
                actionType,
                notification,
                oldSnapshot,
                mapper.notificationSnapshot(notification),
                reason
        );
    }
}
