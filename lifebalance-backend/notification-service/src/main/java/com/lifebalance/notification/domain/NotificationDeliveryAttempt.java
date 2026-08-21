package com.lifebalance.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery_attempts", schema = "notification")
public class NotificationDeliveryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private NotificationRecord notification;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 16)
    private NotificationDeliveryStatus deliveryStatus;

    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "attempted_at", nullable = false)
    private OffsetDateTime attemptedAt;

    protected NotificationDeliveryAttempt() {
    }

    public static NotificationDeliveryAttempt record(
            NotificationRecord notification,
            int attemptNumber,
            NotificationDeliveryStatus deliveryStatus,
            String providerMessageId,
            String errorMessage
    ) {
        NotificationDeliveryAttempt attempt = new NotificationDeliveryAttempt();
        attempt.notification = notification;
        attempt.ownerId = notification.getOwnerId();
        attempt.channel = notification.getChannel();
        attempt.attemptNumber = attemptNumber;
        attempt.deliveryStatus = deliveryStatus;
        attempt.providerMessageId = NotificationRecord.normalizeText(providerMessageId, 200);
        attempt.errorMessage = NotificationRecord.normalizeText(errorMessage, 1000);
        return attempt;
    }

    @PrePersist
    void onCreate() {
        attemptedAt = attemptedAt == null ? OffsetDateTime.now() : attemptedAt;
    }

    public UUID getId() {
        return id;
    }

    public NotificationRecord getNotification() {
        return notification;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public NotificationDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
