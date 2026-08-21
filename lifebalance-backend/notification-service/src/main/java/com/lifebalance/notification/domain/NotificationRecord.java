package com.lifebalance.notification.domain;

import com.lifebalance.notification.error.NotificationExceptions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "notification")
public class NotificationRecord {

    private static final int TITLE_MAX_LENGTH = 200;
    private static final int MESSAGE_MAX_LENGTH = 2000;
    private static final int REFERENCE_TYPE_MAX_LENGTH = 64;
    private static final int PURPOSE_MAX_LENGTH = 500;
    private static final int PROVIDER_MESSAGE_ID_MAX_LENGTH = 200;
    private static final int FAILURE_REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 16)
    private NotificationStatus notificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 16)
    private NotificationDeliveryStatus deliveryStatus;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = MESSAGE_MAX_LENGTH)
    private String message;

    @Column(name = "reference_type", length = REFERENCE_TYPE_MAX_LENGTH)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(nullable = false, length = PURPOSE_MAX_LENGTH)
    private String purpose;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "failure_reason", length = FAILURE_REASON_MAX_LENGTH)
    private String failureReason;

    @Column(name = "provider_message_id", length = PROVIDER_MESSAGE_ID_MAX_LENGTH)
    private String providerMessageId;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected NotificationRecord() {
    }

    public static NotificationRecord create(
            UUID ownerId,
            UUID recipientId,
            UUID actorId,
            NotificationEventType eventType,
            NotificationChannel channel,
            NotificationPriority priority,
            String title,
            String message,
            String referenceType,
            UUID referenceId,
            String purpose,
            OffsetDateTime scheduledAt,
            boolean preferenceEnabled
    ) {
        validateReferencePair(referenceType, referenceId);
        OffsetDateTime now = OffsetDateTime.now();
        NotificationRecord notification = new NotificationRecord();
        notification.ownerId = requireUuid(ownerId, "ownerId is required.");
        notification.recipientId = requireUuid(recipientId, "recipientId is required.");
        notification.actorId = actorId;
        notification.eventType = requireEventType(eventType);
        notification.channel = requireChannel(channel);
        notification.priority = priority == null ? NotificationPriority.NORMAL : priority;
        notification.title = requireText(title, TITLE_MAX_LENGTH, "title is required.");
        notification.message = requireText(message, MESSAGE_MAX_LENGTH, "message is required.");
        notification.referenceType = normalizeText(referenceType, REFERENCE_TYPE_MAX_LENGTH);
        notification.referenceId = referenceId;
        notification.purpose = requireText(purpose, PURPOSE_MAX_LENGTH, "purpose is required.");
        notification.scheduledAt = scheduledAt;
        notification.createdBy = actorId;
        notification.updatedBy = actorId;

        if (!preferenceEnabled) {
            notification.notificationStatus = NotificationStatus.ARCHIVED;
            notification.deliveryStatus = NotificationDeliveryStatus.SKIPPED;
            notification.archivedAt = now;
            return notification;
        }

        notification.notificationStatus = NotificationStatus.UNREAD;
        if (scheduledAt != null && scheduledAt.isAfter(now)) {
            notification.deliveryStatus = NotificationDeliveryStatus.PENDING;
        } else if (channel == NotificationChannel.IN_APP) {
            notification.deliveryStatus = NotificationDeliveryStatus.SENT;
            notification.sentAt = now;
        } else {
            notification.deliveryStatus = NotificationDeliveryStatus.PENDING;
        }
        return notification;
    }

    public void markRead(UUID actorId) {
        ensureNotArchived();
        if (notificationStatus == NotificationStatus.READ) {
            return;
        }
        notificationStatus = NotificationStatus.READ;
        readAt = OffsetDateTime.now();
        updatedBy = actorId;
    }

    public void markUnread(UUID actorId) {
        ensureNotArchived();
        if (notificationStatus == NotificationStatus.UNREAD) {
            return;
        }
        notificationStatus = NotificationStatus.UNREAD;
        readAt = null;
        updatedBy = actorId;
    }

    public void archive(UUID actorId) {
        if (notificationStatus == NotificationStatus.ARCHIVED) {
            return;
        }
        notificationStatus = NotificationStatus.ARCHIVED;
        archivedAt = OffsetDateTime.now();
        updatedBy = actorId;
    }

    public void markSent(UUID actorId, String providerMessageId) {
        ensureDeliverable(NotificationDeliveryStatus.SENT);
        deliveryStatus = NotificationDeliveryStatus.SENT;
        sentAt = OffsetDateTime.now();
        failedAt = null;
        failureReason = null;
        this.providerMessageId = normalizeText(providerMessageId, PROVIDER_MESSAGE_ID_MAX_LENGTH);
        updatedBy = actorId;
    }

    public void markFailed(UUID actorId, String failureReason) {
        ensureDeliverable(NotificationDeliveryStatus.FAILED);
        deliveryStatus = NotificationDeliveryStatus.FAILED;
        failedAt = OffsetDateTime.now();
        this.failureReason = normalizeText(failureReason, FAILURE_REASON_MAX_LENGTH);
        updatedBy = actorId;
    }

    public void retry(UUID actorId) {
        if (deliveryStatus != NotificationDeliveryStatus.FAILED) {
            throw NotificationExceptions.invalidDeliveryState(id, deliveryStatus, NotificationDeliveryStatus.FAILED);
        }
        retryCount++;
        deliveryStatus = NotificationDeliveryStatus.PENDING;
        failedAt = null;
        failureReason = null;
        providerMessageId = null;
        updatedBy = actorId;
    }

    public boolean isUnread() {
        return notificationStatus == NotificationStatus.UNREAD;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? now : updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    static String normalizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw NotificationExceptions.textTooLong(maxLength);
        }
        return normalized;
    }

    private static String requireText(String value, int maxLength, String message) {
        String normalized = normalizeText(value, maxLength);
        if (normalized == null) {
            throw NotificationExceptions.invalidRequest(message);
        }
        return normalized;
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw NotificationExceptions.invalidRequest(message);
        }
        return value;
    }

    private static NotificationEventType requireEventType(NotificationEventType eventType) {
        if (eventType == null) {
            throw NotificationExceptions.invalidRequest("eventType is required.");
        }
        return eventType;
    }

    private static NotificationChannel requireChannel(NotificationChannel channel) {
        if (channel == null) {
            throw NotificationExceptions.invalidRequest("channel is required.");
        }
        return channel;
    }

    private static void validateReferencePair(String referenceType, UUID referenceId) {
        boolean hasReferenceType = normalizeText(referenceType, REFERENCE_TYPE_MAX_LENGTH) != null;
        if (hasReferenceType != (referenceId != null)) {
            throw NotificationExceptions.invalidReference();
        }
    }

    private void ensureNotArchived() {
        if (notificationStatus == NotificationStatus.ARCHIVED) {
            throw NotificationExceptions.invalidState(id, notificationStatus);
        }
    }

    private void ensureDeliverable(NotificationDeliveryStatus nextStatus) {
        if (deliveryStatus == NotificationDeliveryStatus.SKIPPED
                || deliveryStatus == NotificationDeliveryStatus.CANCELLED
                || notificationStatus == NotificationStatus.ARCHIVED) {
            throw NotificationExceptions.invalidDeliveryState(id, deliveryStatus, nextStatus);
        }
        if (nextStatus == NotificationDeliveryStatus.FAILED && deliveryStatus != NotificationDeliveryStatus.PENDING) {
            throw NotificationExceptions.invalidDeliveryState(id, deliveryStatus, NotificationDeliveryStatus.PENDING);
        }
        if (nextStatus == NotificationDeliveryStatus.SENT
                && deliveryStatus != NotificationDeliveryStatus.PENDING
                && deliveryStatus != NotificationDeliveryStatus.FAILED) {
            throw NotificationExceptions.invalidDeliveryState(id, deliveryStatus, NotificationDeliveryStatus.PENDING);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public NotificationDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getPurpose() {
        return purpose;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public OffsetDateTime getFailedAt() {
        return failedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
