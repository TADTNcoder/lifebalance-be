package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.config.NotificationProperties;
import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationPriority;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.domain.NotificationStatus;
import com.lifebalance.notification.dto.BroadcastNotificationRequest;
import com.lifebalance.notification.dto.BulkNotificationActionResponse;
import com.lifebalance.notification.dto.CreateNotificationRequest;
import com.lifebalance.notification.dto.NotificationReasonRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.UnreadCountResponse;
import com.lifebalance.notification.error.NotificationExceptions;
import com.lifebalance.notification.repository.NotificationPreferenceRepository;
import com.lifebalance.notification.repository.NotificationRepository;
import com.lifebalance.notification.service.NotificationService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationDeliveryAttemptRecorder deliveryAttemptRecorder;
    private final NotificationHistoryRecorder historyRecorder;
    private final NotificationMapper mapper;
    private final NotificationProperties properties;

    NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository,
            NotificationDeliveryAttemptRecorder deliveryAttemptRecorder,
            NotificationHistoryRecorder historyRecorder,
            NotificationMapper mapper,
            NotificationProperties properties
    ) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.deliveryAttemptRecorder = deliveryAttemptRecorder;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    @Transactional
    public List<NotificationResponse> create(UUID ownerId, CreateNotificationRequest request) {
        Objects.requireNonNull(request, "Create notification request is required.");
        requirePolicyApproval(request.policyApproved(), request.eventType());
        return createNotifications(
                ownerId,
                List.of(ownerId),
                request.eventType(),
                request.channels(),
                request.priority(),
                request.title(),
                request.message(),
                request.referenceType(),
                request.referenceId(),
                request.purpose(),
                request.scheduledAt(),
                request.reason()
        );
    }

    @Override
    @Transactional
    public List<NotificationResponse> broadcast(UUID actorId, BroadcastNotificationRequest request) {
        Objects.requireNonNull(request, "Broadcast notification request is required.");
        requirePolicyApproval(request.policyApproved(), request.eventType());
        if (request.recipientIds() == null || request.recipientIds().isEmpty()) {
            throw NotificationExceptions.invalidRequest("recipientIds is required.");
        }
        return createNotifications(
                actorId,
                request.recipientIds().stream().sorted(Comparator.naturalOrder()).toList(),
                request.eventType(),
                request.channels(),
                request.priority(),
                request.title(),
                request.message(),
                request.referenceType(),
                request.referenceId(),
                request.purpose(),
                request.scheduledAt(),
                request.reason()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID ownerId, UUID notificationId) {
        return notificationRepository.findByIdAndOwnerId(notificationId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> NotificationExceptions.notificationNotFound(notificationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> search(
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
    ) {
        return notificationRepository.search(
                        ownerId,
                        status,
                        eventType,
                        channel,
                        deliveryStatus,
                        normalizeReferenceType(referenceType),
                        referenceId,
                        from,
                        to,
                        pageable
                )
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID ownerId, UUID notificationId) {
        NotificationRecord notification = findForUpdate(ownerId, notificationId);
        String oldSnapshot = mapper.notificationSnapshot(notification);
        notification.markRead(ownerId);
        notification = notificationRepository.save(notification);
        recordHistory(NotificationHistoryActionType.NOTIFICATION_READ, notification, oldSnapshot, null);
        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markUnread(UUID ownerId, UUID notificationId) {
        NotificationRecord notification = findForUpdate(ownerId, notificationId);
        String oldSnapshot = mapper.notificationSnapshot(notification);
        notification.markUnread(ownerId);
        notification = notificationRepository.save(notification);
        recordHistory(NotificationHistoryActionType.NOTIFICATION_UNREAD, notification, oldSnapshot, null);
        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public BulkNotificationActionResponse markAllRead(UUID ownerId) {
        List<NotificationRecord> unreadNotifications = notificationRepository.search(
                        ownerId,
                        NotificationStatus.UNREAD,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Pageable.unpaged()
                )
                .getContent();
        for (NotificationRecord notification : unreadNotifications) {
            String oldSnapshot = mapper.notificationSnapshot(notification);
            notification.markRead(ownerId);
            recordHistory(NotificationHistoryActionType.NOTIFICATION_READ, notification, oldSnapshot, "mark all read");
        }
        notificationRepository.saveAll(unreadNotifications);
        return new BulkNotificationActionResponse(unreadNotifications.size());
    }

    @Override
    @Transactional
    public NotificationResponse archive(UUID ownerId, UUID notificationId, NotificationReasonRequest request) {
        NotificationRecord notification = findForUpdate(ownerId, notificationId);
        String oldSnapshot = mapper.notificationSnapshot(notification);
        String reason = request == null ? null : request.reason();
        notification.archive(ownerId);
        notification = notificationRepository.save(notification);
        recordHistory(NotificationHistoryActionType.NOTIFICATION_ARCHIVED, notification, oldSnapshot, reason);
        return mapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(UUID ownerId) {
        return new UnreadCountResponse(
                notificationRepository.countByOwnerIdAndNotificationStatus(ownerId, NotificationStatus.UNREAD)
        );
    }

    private List<NotificationResponse> createNotifications(
            UUID actorId,
            List<UUID> recipientIds,
            NotificationEventType eventType,
            Set<NotificationChannel> requestedChannels,
            NotificationPriority priority,
            String title,
            String message,
            String referenceType,
            UUID referenceId,
            String purpose,
            OffsetDateTime scheduledAt,
            String reason
    ) {
        validateReference(referenceType, referenceId);
        List<NotificationResponse> responses = new ArrayList<>();
        for (UUID recipientId : recipientIds) {
            if (recipientId == null) {
                throw NotificationExceptions.invalidRequest("recipientIds must not contain null values.");
            }
            for (NotificationChannel channel : normalizeChannels(requestedChannels)) {
                boolean preferenceEnabled = isPreferenceEnabled(recipientId, eventType, channel);
                NotificationRecord notification = NotificationRecord.create(
                        recipientId,
                        recipientId,
                        actorId,
                        eventType,
                        channel,
                        priority,
                        title,
                        message,
                        referenceType,
                        referenceId,
                        purpose,
                        scheduledAt,
                        preferenceEnabled
                );
                notification = notificationRepository.save(notification);
                recordCreation(notification, reason);
                responses.add(mapper.toResponse(notification));
            }
        }
        return responses;
    }

    private void requirePolicyApproval(Boolean policyApproved, NotificationEventType eventType) {
        if (!Boolean.TRUE.equals(policyApproved)) {
            throw NotificationExceptions.policyNotApproved(eventType);
        }
    }

    private void validateReference(String referenceType, UUID referenceId) {
        if ((normalizeReferenceType(referenceType) == null) != (referenceId == null)) {
            throw NotificationExceptions.invalidReference();
        }
    }

    private String normalizeReferenceType(String referenceType) {
        if (referenceType == null || referenceType.isBlank()) {
            return null;
        }
        String normalized = referenceType.trim();
        if (normalized.length() > 64) {
            throw NotificationExceptions.textTooLong(64);
        }
        return normalized;
    }

    private EnumSet<NotificationChannel> normalizeChannels(Set<NotificationChannel> requestedChannels) {
        if (requestedChannels == null || requestedChannels.isEmpty()) {
            return EnumSet.of(properties.getDefaultChannel());
        }
        EnumSet<NotificationChannel> channels = EnumSet.noneOf(NotificationChannel.class);
        requestedChannels.stream()
                .filter(Objects::nonNull)
                .forEach(channels::add);
        if (channels.isEmpty()) {
            throw NotificationExceptions.invalidRequest("channels must contain at least one supported channel.");
        }
        return channels;
    }

    private boolean isPreferenceEnabled(
            UUID ownerId,
            NotificationEventType eventType,
            NotificationChannel channel
    ) {
        return preferenceRepository.findByOwnerIdAndEventTypeAndChannel(ownerId, eventType, channel)
                .map(preference -> preference.isEnabled())
                .orElse(true);
    }

    private NotificationRecord findForUpdate(UUID ownerId, UUID notificationId) {
        return notificationRepository.findByIdAndOwnerIdForUpdate(notificationId, ownerId)
                .orElseThrow(() -> NotificationExceptions.notificationNotFound(notificationId));
    }

    private void recordCreation(NotificationRecord notification, String reason) {
        String snapshot = mapper.notificationSnapshot(notification);
        historyRecorder.record(
                notification.getOwnerId(),
                notification.getActorId(),
                NotificationHistoryActionType.NOTIFICATION_CREATED,
                notification,
                null,
                snapshot,
                reason
        );
        recordDeliveryHistory(notification, reason);
        deliveryAttemptRecorder.record(
                notification,
                notification.getDeliveryStatus(),
                notification.getProviderMessageId(),
                notification.getFailureReason()
        );
    }

    private void recordDeliveryHistory(NotificationRecord notification, String reason) {
        NotificationHistoryActionType actionType = switch (notification.getDeliveryStatus()) {
            case SENT -> NotificationHistoryActionType.NOTIFICATION_SENT;
            case PENDING -> NotificationHistoryActionType.NOTIFICATION_QUEUED;
            case SKIPPED -> NotificationHistoryActionType.NOTIFICATION_SKIPPED;
            case FAILED -> NotificationHistoryActionType.NOTIFICATION_FAILED;
            case CANCELLED -> NotificationHistoryActionType.NOTIFICATION_ARCHIVED;
        };
        historyRecorder.record(
                notification.getOwnerId(),
                notification.getActorId(),
                actionType,
                notification,
                null,
                mapper.notificationSnapshot(notification),
                reason
        );
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
