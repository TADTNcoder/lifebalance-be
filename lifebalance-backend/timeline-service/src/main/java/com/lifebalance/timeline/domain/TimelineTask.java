package com.lifebalance.timeline.domain;

import com.lifebalance.timeline.error.TimelineExceptions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "timeline_tasks", schema = "timeline")
public class TimelineTask {

    private static final int TITLE_MAX_LENGTH = 255;
    private static final Set<TimelineTaskStatus> TIMELINE_ELIGIBLE_STATUSES = EnumSet.of(
            TimelineTaskStatus.DRAFT,
            TimelineTaskStatus.PLANNED,
            TimelineTaskStatus.SCHEDULED
    );

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false, length = 32)
    private TimelineTaskStatus taskStatus;

    @Column(name = "has_time_capital", nullable = false)
    private boolean hasTimeCapital;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "capital_cycle_id")
    private UUID capitalCycleId;

    @Column(name = "cycle_start_at")
    private OffsetDateTime cycleStartAt;

    @Column(name = "cycle_end_at")
    private OffsetDateTime cycleEndAt;

    @Column(name = "scheduled_start_at")
    private OffsetDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at")
    private OffsetDateTime scheduledEndAt;

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

    protected TimelineTask() {
    }

    public static TimelineTask register(
            UUID ownerId,
            UUID actorId,
            UUID taskId,
            String title,
            TimelineTaskStatus taskStatus,
            boolean hasTimeCapital,
            Integer estimatedMinutes,
            LocalDate deadline,
            UUID capitalCycleId,
            OffsetDateTime cycleStartAt,
            OffsetDateTime cycleEndAt,
            OffsetDateTime scheduledStartAt,
            OffsetDateTime scheduledEndAt
    ) {
        TimelineTask task = new TimelineTask();
        task.id = taskId;
        task.ownerId = ownerId;
        task.createdBy = actorId;
        task.applySnapshot(
                actorId,
                title,
                taskStatus,
                hasTimeCapital,
                estimatedMinutes,
                deadline,
                capitalCycleId,
                cycleStartAt,
                cycleEndAt,
                scheduledStartAt,
                scheduledEndAt
        );
        return task;
    }

    public void applySnapshot(
            UUID actorId,
            String title,
            TimelineTaskStatus taskStatus,
            boolean hasTimeCapital,
            Integer estimatedMinutes,
            LocalDate deadline,
            UUID capitalCycleId,
            OffsetDateTime cycleStartAt,
            OffsetDateTime cycleEndAt,
            OffsetDateTime scheduledStartAt,
            OffsetDateTime scheduledEndAt
    ) {
        this.updatedBy = actorId;
        this.title = requireTitle(title);
        this.taskStatus = taskStatus == null ? TimelineTaskStatus.DRAFT : taskStatus;
        this.hasTimeCapital = hasTimeCapital;
        this.estimatedMinutes = requirePositiveEstimatedMinutes(estimatedMinutes);
        this.deadline = deadline;
        this.capitalCycleId = capitalCycleId;
        this.cycleStartAt = cycleStartAt;
        this.cycleEndAt = cycleEndAt;
        validateCycleWindow(cycleStartAt, cycleEndAt);
        applyScheduledWindow(scheduledStartAt, scheduledEndAt);
    }

    public boolean isTimelineEligible() {
        return TIMELINE_ELIGIBLE_STATUSES.contains(taskStatus)
                && (hasTimeCapital || estimatedMinutes != null);
    }

    public boolean isVisibleOnMainTimeline() {
        return taskStatus != TimelineTaskStatus.CANCELLED
                && taskStatus != TimelineTaskStatus.ARCHIVED;
    }

    public void markScheduled(
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            UUID actorId
    ) {
        validateScheduledWindow(startAt, endAt);
        taskStatus = TimelineTaskStatus.SCHEDULED;
        scheduledStartAt = startAt;
        scheduledEndAt = endAt;
        updatedBy = actorId;
    }

    public void clearScheduleIfOnlyScheduled(UUID actorId) {
        if (taskStatus == TimelineTaskStatus.SCHEDULED) {
            taskStatus = TimelineTaskStatus.PLANNED;
        }
        scheduledStartAt = null;
        scheduledEndAt = null;
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

    private static String requireTitle(String value) {
        String normalized = normalizeText(value, TITLE_MAX_LENGTH);
        if (normalized == null) {
            throw TimelineExceptions.invalidTaskSnapshot("title is required.");
        }
        return normalized;
    }

    private static Integer requirePositiveEstimatedMinutes(Integer value) {
        if (value != null && value <= 0) {
            throw TimelineExceptions.invalidTaskSnapshot("estimatedMinutes must be greater than 0 when provided.");
        }
        return value;
    }

    private static void validateCycleWindow(OffsetDateTime cycleStartAt, OffsetDateTime cycleEndAt) {
        if ((cycleStartAt == null) != (cycleEndAt == null)) {
            throw TimelineExceptions.invalidCycle("cycleStartAt and cycleEndAt must be provided together.");
        }
        if (cycleStartAt != null && !cycleStartAt.isBefore(cycleEndAt)) {
            throw TimelineExceptions.invalidCycle("cycleStartAt must be before cycleEndAt.");
        }
    }

    private static void validateScheduledWindow(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw TimelineExceptions.invalidWindow();
        }
    }

    private void applyScheduledWindow(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null && endAt == null) {
            scheduledStartAt = null;
            scheduledEndAt = null;
            return;
        }
        validateScheduledWindow(startAt, endAt);
        scheduledStartAt = startAt;
        scheduledEndAt = endAt;
    }

    static String normalizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw TimelineExceptions.invalidTextLength(maxLength);
        }
        return normalized;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public TimelineTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public boolean isHasTimeCapital() {
        return hasTimeCapital;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public UUID getCapitalCycleId() {
        return capitalCycleId;
    }

    public OffsetDateTime getCycleStartAt() {
        return cycleStartAt;
    }

    public OffsetDateTime getCycleEndAt() {
        return cycleEndAt;
    }

    public OffsetDateTime getScheduledStartAt() {
        return scheduledStartAt;
    }

    public OffsetDateTime getScheduledEndAt() {
        return scheduledEndAt;
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
