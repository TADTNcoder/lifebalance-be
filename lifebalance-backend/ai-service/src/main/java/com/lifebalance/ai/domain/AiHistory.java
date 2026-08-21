package com.lifebalance.ai.domain;

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
@Table(name = "ai_histories", schema = "ai")
public class AiHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private AiHistoryActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private AiConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private AiMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id")
    private AiRecommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insight_id")
    private AiInsight insight;

    @Column(name = "old_value", length = 4000)
    private String oldValue;

    @Column(name = "new_value", length = 4000)
    private String newValue;

    @Column(length = 1000)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected AiHistory() {
    }

    public static AiHistory record(
            UUID ownerId,
            UUID actorId,
            AiHistoryActionType actionType,
            AiConversation conversation,
            AiMessage message,
            AiRecommendation recommendation,
            AiInsight insight,
            String oldValue,
            String newValue,
            String reason
    ) {
        AiHistory history = new AiHistory();
        history.ownerId = ownerId;
        history.actorId = actorId;
        history.actionType = actionType;
        history.conversation = conversation;
        history.message = message;
        history.recommendation = recommendation;
        history.insight = insight;
        history.oldValue = AiText.normalize(oldValue, 4000);
        history.newValue = AiText.normalize(newValue, 4000);
        history.reason = AiText.normalize(reason, 1000);
        return history;
    }

    @PrePersist
    void onCreate() {
        occurredAt = occurredAt == null ? OffsetDateTime.now() : occurredAt;
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

    public AiHistoryActionType getActionType() {
        return actionType;
    }

    public AiConversation getConversation() {
        return conversation;
    }

    public AiMessage getMessage() {
        return message;
    }

    public AiRecommendation getRecommendation() {
        return recommendation;
    }

    public AiInsight getInsight() {
        return insight;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
