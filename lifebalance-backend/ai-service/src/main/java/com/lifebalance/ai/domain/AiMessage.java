package com.lifebalance.ai.domain;

import com.lifebalance.ai.error.AiExceptions;
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
@Table(name = "ai_messages", schema = "ai")
public class AiMessage {

    public static final int CONTENT_MAX_LENGTH = 4000;
    public static final int MODEL_NAME_MAX_LENGTH = 120;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_role", nullable = false, length = 16)
    private AiMessageRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AiIntent intent;

    @Column(nullable = false, length = CONTENT_MAX_LENGTH)
    private String content;

    @Column(name = "model_name", length = MODEL_NAME_MAX_LENGTH)
    private String modelName;

    @Column(name = "token_estimate", nullable = false)
    private Integer tokenEstimate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AiMessage() {
    }

    public static AiMessage record(
            AiConversation conversation,
            UUID ownerId,
            UUID actorId,
            AiMessageRole role,
            AiIntent intent,
            String content,
            String modelName
    ) {
        if (conversation == null) {
            throw AiExceptions.invalidRequest("conversation is required.");
        }
        AiMessage message = new AiMessage();
        message.conversation = conversation;
        message.ownerId = requireUuid(ownerId, "ownerId is required.");
        message.actorId = actorId;
        message.role = role == null ? AiMessageRole.USER : role;
        message.intent = intent == null ? conversation.getIntent() : intent;
        message.content = AiText.require(content, CONTENT_MAX_LENGTH, "content is required.");
        message.modelName = AiText.normalize(modelName, MODEL_NAME_MAX_LENGTH);
        message.tokenEstimate = estimateTokens(message.content);
        return message;
    }

    @PrePersist
    void onCreate() {
        createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
    }

    static int estimateTokens(String content) {
        String normalized = AiText.normalize(content, CONTENT_MAX_LENGTH);
        if (normalized == null) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(normalized.length() / 4.0));
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw AiExceptions.invalidRequest(message);
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public AiConversation getConversation() {
        return conversation;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public AiMessageRole getRole() {
        return role;
    }

    public AiIntent getIntent() {
        return intent;
    }

    public String getContent() {
        return content;
    }

    public String getModelName() {
        return modelName;
    }

    public Integer getTokenEstimate() {
        return tokenEstimate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
