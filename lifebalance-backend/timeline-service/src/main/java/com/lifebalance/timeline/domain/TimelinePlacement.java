package com.lifebalance.timeline.domain;

import com.lifebalance.timeline.error.TimelineExceptions;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "timeline_placements", schema = "timeline")
@SQLRestriction("deleted_at IS NULL")
public class TimelinePlacement {

    private static final int TIMEZONE_MAX_LENGTH = 64;
    private static final int REASON_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TimelineTask task;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(length = TIMEZONE_MAX_LENGTH)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TimelinePlacementSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TimelinePlacementStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_policy", nullable = false, length = 32)
    private TimelineConflictPolicy conflictPolicy;

    @Column(name = "conflict_confirmed", nullable = false)
    private boolean conflictConfirmed;

    @Column(name = "is_conflicted", nullable = false)
    private boolean conflicted;

    @Column(name = "conflict_reason", length = REASON_MAX_LENGTH)
    private String conflictReason;

    @Column(length = REASON_MAX_LENGTH)
    private String reason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected TimelinePlacement() {
    }

    public static TimelinePlacement schedule(
            UUID ownerId,
            UUID actorId,
            TimelineTask task,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String timezone,
            TimelinePlacementSource source,
            TimelineConflictPolicy conflictPolicy,
            boolean conflicted,
            boolean conflictConfirmed,
            String conflictReason,
            String reason
    ) {
        TimelinePlacement placement = new TimelinePlacement();
        placement.ownerId = ownerId;
        placement.task = task;
        placement.status = TimelinePlacementStatus.ACTIVE;
        placement.createdBy = actorId;
        placement.applyWindow(
                actorId,
                startAt,
                endAt,
                timezone,
                source,
                conflictPolicy,
                conflicted,
                conflictConfirmed,
                conflictReason,
                reason
        );
        return placement;
    }

    public void reschedule(
            UUID actorId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String timezone,
            TimelinePlacementSource source,
            TimelineConflictPolicy conflictPolicy,
            boolean conflicted,
            boolean conflictConfirmed,
            String conflictReason,
            String reason
    ) {
        ensureActive();
        applyWindow(
                actorId,
                startAt,
                endAt,
                timezone,
                source,
                conflictPolicy,
                conflicted,
                conflictConfirmed,
                conflictReason,
                reason
        );
    }

    public void cancel(UUID actorId, String reason) {
        ensureActive();
        status = TimelinePlacementStatus.CANCELLED;
        this.reason = TimelineTask.normalizeText(reason, REASON_MAX_LENGTH);
        updatedBy = actorId;
    }

    public void archive(UUID actorId, String reason) {
        ensureActive();
        status = TimelinePlacementStatus.ARCHIVED;
        this.reason = TimelineTask.normalizeText(reason, REASON_MAX_LENGTH);
        deletedAt = OffsetDateTime.now();
        updatedBy = actorId;
    }

    public boolean isActive() {
        return status == TimelinePlacementStatus.ACTIVE;
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

    private void applyWindow(
            UUID actorId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String timezone,
            TimelinePlacementSource source,
            TimelineConflictPolicy conflictPolicy,
            boolean conflicted,
            boolean conflictConfirmed,
            String conflictReason,
            String reason
    ) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw TimelineExceptions.invalidWindow();
        }
        this.startAt = startAt;
        this.endAt = endAt;
        this.timezone = TimelineTask.normalizeText(timezone, TIMEZONE_MAX_LENGTH);
        this.source = source == null ? TimelinePlacementSource.MANUAL : source;
        this.conflictPolicy = conflictPolicy == null ? TimelineConflictPolicy.REJECT : conflictPolicy;
        this.conflicted = conflicted;
        this.conflictConfirmed = conflicted && conflictConfirmed;
        this.conflictReason = this.conflictConfirmed
                ? TimelineTask.normalizeText(conflictReason, REASON_MAX_LENGTH)
                : null;
        this.reason = TimelineTask.normalizeText(reason, REASON_MAX_LENGTH);
        this.updatedBy = actorId;
    }

    private void ensureActive() {
        if (!isActive()) {
            throw TimelineExceptions.placementNotActive(id);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public TimelineTask getTask() {
        return task;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public TimelinePlacementSource getSource() {
        return source;
    }

    public TimelinePlacementStatus getStatus() {
        return status;
    }

    public TimelineConflictPolicy getConflictPolicy() {
        return conflictPolicy;
    }

    public boolean isConflictConfirmed() {
        return conflictConfirmed;
    }

    public boolean isConflicted() {
        return conflicted;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public String getReason() {
        return reason;
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
