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
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences", schema = "notification")
public class NotificationPreference {

    private static final int TIMEZONE_MAX_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(length = TIMEZONE_MAX_LENGTH)
    private String timezone;

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

    protected NotificationPreference() {
    }

    public static NotificationPreference create(
            UUID ownerId,
            UUID actorId,
            NotificationEventType eventType,
            NotificationChannel channel,
            boolean enabled,
            LocalTime quietHoursStart,
            LocalTime quietHoursEnd,
            String timezone
    ) {
        NotificationPreference preference = new NotificationPreference();
        preference.ownerId = ownerId;
        preference.createdBy = actorId;
        preference.eventType = requireEventType(eventType);
        preference.channel = requireChannel(channel);
        preference.update(actorId, enabled, quietHoursStart, quietHoursEnd, timezone);
        return preference;
    }

    public void update(
            UUID actorId,
            boolean enabled,
            LocalTime quietHoursStart,
            LocalTime quietHoursEnd,
            String timezone
    ) {
        validateQuietHours(quietHoursStart, quietHoursEnd);
        this.enabled = enabled;
        this.quietHoursStart = quietHoursStart;
        this.quietHoursEnd = quietHoursEnd;
        this.timezone = normalizeTimezone(timezone);
        this.updatedBy = actorId;
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

    private static void validateQuietHours(LocalTime quietHoursStart, LocalTime quietHoursEnd) {
        if ((quietHoursStart == null) != (quietHoursEnd == null)) {
            throw NotificationExceptions.invalidRequest("quietHoursStart and quietHoursEnd must be provided together.");
        }
    }

    private static String normalizeTimezone(String value) {
        String normalized = NotificationRecord.normalizeText(value, TIMEZONE_MAX_LENGTH);
        if (normalized == null) {
            return null;
        }
        try {
            ZoneId.of(normalized);
            return normalized;
        } catch (DateTimeException exception) {
            throw NotificationExceptions.invalidTimezone(normalized);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }

    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public String getTimezone() {
        return timezone;
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
