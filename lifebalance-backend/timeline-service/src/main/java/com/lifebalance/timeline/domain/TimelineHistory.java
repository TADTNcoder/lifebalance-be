package com.lifebalance.timeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "timeline_histories", schema = "timeline")
public class TimelineHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private TimelineHistoryActionType actionType;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "placement_id")
    private TimelinePlacement placement;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TimelineTask task;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(length = 1000)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected TimelineHistory() {
    }

    public static TimelineHistory record(
            UUID ownerId,
            UUID actorId,
            TimelineHistoryActionType actionType,
            TimelinePlacement placement,
            TimelineTask task,
            String oldValue,
            String newValue,
            String reason
    ) {
        TimelineHistory history = new TimelineHistory();
        history.ownerId = ownerId;
        history.actorId = actorId;
        history.actionType = actionType;
        history.placement = placement;
        history.task = task;
        history.oldValue = oldValue;
        history.newValue = newValue;
        history.reason = TimelineTask.normalizeText(reason, 1000);
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

    public TimelineHistoryActionType getActionType() {
        return actionType;
    }

    public TimelinePlacement getPlacement() {
        return placement;
    }

    public TimelineTask getTask() {
        return task;
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
