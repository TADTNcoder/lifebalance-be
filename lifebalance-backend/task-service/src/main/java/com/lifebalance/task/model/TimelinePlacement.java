package com.lifebalance.task.model;

import com.lifebalance.task.model.enums.TimelinePlacementStatus;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timeline_placements", schema = "task")
@SQLDelete(sql = """
        UPDATE task.timeline_placements
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP,
            status = 'ARCHIVED'
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class TimelinePlacement extends BaseAuditableEntity {

    private static final int TIMEZONE_MAX_LENGTH = 64;
    private static final int SOURCE_MAX_LENGTH = 32;
    private static final int REASON_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Size(max = TIMEZONE_MAX_LENGTH)
    @Column(length = TIMEZONE_MAX_LENGTH)
    private String timezone;

    @NotNull
    @Size(max = SOURCE_MAX_LENGTH)
    @Column(nullable = false, length = SOURCE_MAX_LENGTH)
    @Builder.Default
    private String source = "MANUAL";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private TimelinePlacementStatus status = TimelinePlacementStatus.ACTIVE;

    @Size(max = REASON_MAX_LENGTH)
    @Column(length = REASON_MAX_LENGTH)
    private String reason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    @PreUpdate
    void normalizeBeforeSave() {
        if (ownerId == null && userId != null) {
            ownerId = userId;
        } else if (userId == null && ownerId != null) {
            userId = ownerId;
        }
        if (status == null) {
            status = TimelinePlacementStatus.ACTIVE;
        }
        if (source == null || source.isBlank()) {
            source = "MANUAL";
        } else {
            source = source.trim().toUpperCase();
        }
        if (timezone != null) {
            timezone = normalizeOptionalText(timezone, TIMEZONE_MAX_LENGTH);
        }
        if (reason != null) {
            reason = normalizeOptionalText(reason, REASON_MAX_LENGTH);
        }
        requireChronologicalWindow(startAt, endAt);
    }

    public boolean belongsTo(UUID userId) {
        return userId != null && Objects.equals(ownerId, userId);
    }

    public boolean isActive() {
        return status == TimelinePlacementStatus.ACTIVE;
    }

    public void reschedule(
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String timezone,
            String reason,
            UUID actorId) {

        ensureActive();
        requireChronologicalWindow(startAt, endAt);
        this.startAt = startAt;
        this.endAt = endAt;
        this.timezone = normalizeOptionalText(timezone, TIMEZONE_MAX_LENGTH);
        this.reason = normalizeOptionalText(reason, REASON_MAX_LENGTH);
        this.updatedBy = actorId;
    }

    public void cancel(
            String reason,
            UUID actorId) {

        ensureActive();
        this.status = TimelinePlacementStatus.CANCELLED;
        this.reason = normalizeOptionalText(reason, REASON_MAX_LENGTH);
        this.updatedBy = actorId;
    }

    private void ensureActive() {
        if (!isActive()) {
            throw new IllegalStateException("Timeline placement is not active.");
        }
    }

    private void requireChronologicalWindow(
            OffsetDateTime startAt,
            OffsetDateTime endAt) {

        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("Timeline placement start time must be before end time.");
        }
    }

    private String normalizeOptionalText(
            String value,
            int maxLength) {

        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Timeline placement text must not exceed " + maxLength + " characters.");
        }
        return normalized;
    }
}
