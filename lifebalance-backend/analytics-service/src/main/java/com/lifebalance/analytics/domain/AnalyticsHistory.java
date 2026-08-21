package com.lifebalance.analytics.domain;

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
@Table(name = "analytics_histories", schema = "analytics")
public class AnalyticsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private AnalyticsHistoryActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_record_id")
    private ActualRecord actualRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_result_id")
    private EvaluationResult evaluationResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private AnalyticsReport report;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(length = 1000)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected AnalyticsHistory() {
    }

    public static AnalyticsHistory record(
            UUID ownerId,
            UUID actorId,
            AnalyticsHistoryActionType actionType,
            ActualRecord actualRecord,
            EvaluationResult evaluationResult,
            AnalyticsReport report,
            String oldValue,
            String newValue,
            String reason
    ) {
        AnalyticsHistory history = new AnalyticsHistory();
        history.ownerId = ownerId;
        history.actorId = actorId;
        history.actionType = actionType;
        history.actualRecord = actualRecord;
        history.evaluationResult = evaluationResult;
        history.report = report;
        history.oldValue = oldValue;
        history.newValue = newValue;
        history.reason = ActualRecord.normalizeText(reason, 1000);
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

    public AnalyticsHistoryActionType getActionType() {
        return actionType;
    }

    public ActualRecord getActualRecord() {
        return actualRecord;
    }

    public EvaluationResult getEvaluationResult() {
        return evaluationResult;
    }

    public AnalyticsReport getReport() {
        return report;
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
