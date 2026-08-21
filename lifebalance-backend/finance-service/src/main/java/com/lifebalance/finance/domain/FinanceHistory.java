package com.lifebalance.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "finance_histories", schema = "finance")
public class FinanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private FinanceHistoryActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 64)
    private FinanceReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(length = 1000)
    private String reason;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected FinanceHistory() {
    }

    public static FinanceHistory record(
            UUID ownerId,
            UUID actorId,
            FinanceHistoryActionType actionType,
            FinanceReferenceType referenceType,
            UUID referenceId,
            String reason,
            String oldValue,
            String newValue
    ) {
        FinanceHistory history = new FinanceHistory();
        history.ownerId = ownerId;
        history.actorId = actorId;
        history.actionType = actionType;
        history.referenceType = referenceType;
        history.referenceId = referenceId;
        history.reason = reason;
        history.oldValue = oldValue;
        history.newValue = newValue;
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

    public FinanceHistoryActionType getActionType() {
        return actionType;
    }

    public FinanceReferenceType getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getReason() {
        return reason;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
