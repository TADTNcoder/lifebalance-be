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
@Table(name = "notification_templates", schema = "notification")
public class NotificationTemplate {

    private static final int TEMPLATE_KEY_MAX_LENGTH = 120;
    private static final int TITLE_TEMPLATE_MAX_LENGTH = 200;
    private static final int MESSAGE_TEMPLATE_MAX_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "template_key", nullable = false, length = TEMPLATE_KEY_MAX_LENGTH)
    private String templateKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    @Column(name = "title_template", nullable = false, length = TITLE_TEMPLATE_MAX_LENGTH)
    private String titleTemplate;

    @Column(name = "message_template", nullable = false, length = MESSAGE_TEMPLATE_MAX_LENGTH)
    private String messageTemplate;

    @Column(nullable = false)
    private boolean enabled;

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

    protected NotificationTemplate() {
    }

    public static NotificationTemplate create(
            UUID ownerId,
            UUID actorId,
            String templateKey,
            NotificationEventType eventType,
            NotificationChannel channel,
            String titleTemplate,
            String messageTemplate,
            boolean enabled
    ) {
        NotificationTemplate template = new NotificationTemplate();
        template.ownerId = requireUuid(ownerId, "ownerId is required.");
        template.createdBy = actorId;
        template.templateKey = requireText(templateKey, TEMPLATE_KEY_MAX_LENGTH, "templateKey is required.");
        template.eventType = requireEventType(eventType);
        template.channel = requireChannel(channel);
        template.update(actorId, titleTemplate, messageTemplate, enabled);
        return template;
    }

    public void update(UUID actorId, String titleTemplate, String messageTemplate, boolean enabled) {
        this.titleTemplate = requireText(titleTemplate, TITLE_TEMPLATE_MAX_LENGTH, "titleTemplate is required.");
        this.messageTemplate = requireText(messageTemplate, MESSAGE_TEMPLATE_MAX_LENGTH, "messageTemplate is required.");
        this.enabled = enabled;
        this.updatedBy = actorId;
    }

    public void archive(UUID actorId) {
        enabled = false;
        updatedBy = actorId;
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

    private static String requireText(String value, int maxLength, String message) {
        String normalized = NotificationRecord.normalizeText(value, maxLength);
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

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public boolean isEnabled() {
        return enabled;
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
