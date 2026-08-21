package com.lifebalance.ai.domain;

import com.lifebalance.ai.error.AiExceptions;
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
@Table(name = "ai_conversations", schema = "ai")
public class AiConversation {

    public static final int TITLE_MAX_LENGTH = 200;
    public static final int CONTEXT_TYPE_MAX_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AiIntent intent;

    @Column(name = "context_type", length = CONTEXT_TYPE_MAX_LENGTH)
    private String contextType;

    @Column(name = "context_id")
    private UUID contextId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AiConversationStatus status;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

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

    protected AiConversation() {
    }

    public static AiConversation start(
            UUID ownerId,
            UUID actorId,
            String title,
            AiIntent intent,
            String contextType,
            UUID contextId
    ) {
        validateContextPair(contextType, contextId);
        AiConversation conversation = new AiConversation();
        conversation.ownerId = requireUuid(ownerId, "ownerId is required.");
        conversation.actorId = actorId;
        conversation.title = AiText.require(title, TITLE_MAX_LENGTH, "title is required.");
        conversation.intent = intent == null ? AiIntent.GENERAL : intent;
        conversation.contextType = AiText.normalize(contextType, CONTEXT_TYPE_MAX_LENGTH);
        conversation.contextId = contextId;
        conversation.status = AiConversationStatus.ACTIVE;
        conversation.createdBy = actorId;
        conversation.updatedBy = actorId;
        return conversation;
    }

    public void touch(UUID actorId) {
        ensureActive();
        this.actorId = actorId;
        this.updatedBy = actorId;
    }

    public void archive(UUID actorId) {
        ensureActive();
        status = AiConversationStatus.ARCHIVED;
        archivedAt = OffsetDateTime.now();
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

    public static void validateContextPair(String contextType, UUID contextId) {
        boolean hasContextType = AiText.normalize(contextType, CONTEXT_TYPE_MAX_LENGTH) != null;
        boolean hasContextId = contextId != null;
        if (hasContextType != hasContextId) {
            throw AiExceptions.invalidRequest("contextType and contextId must be provided together.");
        }
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw AiExceptions.invalidRequest(message);
        }
        return value;
    }

    private void ensureActive() {
        if (status != AiConversationStatus.ACTIVE) {
            throw AiExceptions.invalidState(id, String.valueOf(status));
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getTitle() {
        return title;
    }

    public AiIntent getIntent() {
        return intent;
    }

    public String getContextType() {
        return contextType;
    }

    public UUID getContextId() {
        return contextId;
    }

    public AiConversationStatus getStatus() {
        return status;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
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
