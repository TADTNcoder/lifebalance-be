package com.lifebalance.task.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks", schema = "task")
@SQLDelete(sql = """
        UPDATE task.tasks
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class Task extends BaseAuditableEntity {

    private static final int NAME_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final BigDecimal ZERO_COST = BigDecimal.ZERO;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private TaskStatus status = TaskStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PriorityLevel priority;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "planned_start_at")
    private OffsetDateTime plannedStartAt;

    @Column(name = "planned_end_at")
    private OffsetDateTime plannedEndAt;

    @Column(name = "scheduled_start_at")
    private OffsetDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at")
    private OffsetDateTime scheduledEndAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @PositiveOrZero
    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @PositiveOrZero
    @Column(name = "estimated_cost", precision = 19, scale = 4)
    private BigDecimal estimatedCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TaskTag> taskTags = new HashSet<>();

    @PrePersist
    @PreUpdate
    void normalizeBeforeSave() {
        if (ownerId == null && userId != null) {
            ownerId = userId;
        } else if (userId == null && ownerId != null) {
            userId = ownerId;
        }
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = normalizeOptionalText(description);
        }
        if (status == null) {
            status = TaskStatus.DRAFT;
        }
        if (progress == null) {
            progress = 0;
        }
    }

    public void updateDetails(
            String name,
            String description,
            PriorityLevel priority,
            LocalDate deadline,
            Integer estimatedMinutes,
            BigDecimal estimatedCost,
            Category category) {
        ensurePlanningEditable();
        this.name = requireName(name);
        this.description = optionalText(description, "description", DESCRIPTION_MAX_LENGTH);
        this.priority = priority;
        this.deadline = deadline;
        setEstimatedMinutes(estimatedMinutes);
        setEstimatedCost(estimatedCost);
        this.category = category;
    }

    public void plan(
            PriorityLevel priority,
            LocalDate deadline,
            Integer estimatedMinutes,
            BigDecimal estimatedCost,
            Category category) {
        ensureStatusAllows("plan", TaskStatus.DRAFT, TaskStatus.PLANNED);
        updateDetails(name, description, priority, deadline, estimatedMinutes, estimatedCost, category);
        this.status = TaskStatus.PLANNED;
    }

    public void schedule(LocalDate deadline, Integer estimatedMinutes) {
        ensureStatusAllows("schedule", TaskStatus.DRAFT, TaskStatus.PLANNED, TaskStatus.SCHEDULED);
        if (deadline == null) {
            throw new IllegalStateException("Task deadline is required before scheduling.");
        }
        if (estimatedMinutes == null || estimatedMinutes <= 0) {
            throw new IllegalStateException("Task estimatedMinutes must be greater than 0 before scheduling.");
        }
        this.deadline = deadline;
        this.estimatedMinutes = estimatedMinutes;
        this.status = TaskStatus.SCHEDULED;
    }

    public void schedule(
            OffsetDateTime scheduledStartAt,
            OffsetDateTime scheduledEndAt) {

        ensureStatusAllows("schedule", TaskStatus.DRAFT, TaskStatus.PLANNED, TaskStatus.SCHEDULED);
        requireChronologicalWindow(scheduledStartAt, scheduledEndAt, "scheduled");
        if (estimatedMinutes == null || estimatedMinutes <= 0) {
            throw new IllegalStateException("Task estimatedMinutes must be greater than 0 before scheduling.");
        }

        this.deadline = scheduledEndAt.toLocalDate();
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.status = TaskStatus.SCHEDULED;
    }

    public void planWindow(
            OffsetDateTime plannedStartAt,
            OffsetDateTime plannedEndAt) {

        ensurePlanningEditable();
        if (plannedStartAt != null || plannedEndAt != null) {
            requireChronologicalWindow(plannedStartAt, plannedEndAt, "planned");
        }
        this.plannedStartAt = plannedStartAt;
        this.plannedEndAt = plannedEndAt;
    }

    public void start() {
        ensureStatusAllows("start", TaskStatus.PLANNED, TaskStatus.SCHEDULED, TaskStatus.ON_HOLD);
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void pause() {
        ensureStatusAllows("pause", TaskStatus.IN_PROGRESS, TaskStatus.SCHEDULED, TaskStatus.PLANNED);
        this.status = TaskStatus.ON_HOLD;
    }

    public void resume() {
        ensureStatusAllows("resume", TaskStatus.ON_HOLD);
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void markAsCompleted() {
        ensureStatusAllows(
                "complete",
                TaskStatus.PLANNED,
                TaskStatus.SCHEDULED,
                TaskStatus.IN_PROGRESS,
                TaskStatus.ON_HOLD);
        this.status = TaskStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    public void cancel() {
        ensureStatusAllows(
                "cancel",
                TaskStatus.DRAFT,
                TaskStatus.PLANNED,
                TaskStatus.SCHEDULED,
                TaskStatus.IN_PROGRESS,
                TaskStatus.ON_HOLD);
        this.status = TaskStatus.CANCELLED;
        this.cancelledAt = OffsetDateTime.now();
    }

    public void reopen() {
        ensureStatusAllows("reopen", TaskStatus.COMPLETED, TaskStatus.CANCELLED);
        this.status = TaskStatus.PLANNED;
        this.completedAt = null;
        this.cancelledAt = null;
    }

    public void archive() {
        if (status == TaskStatus.ARCHIVED) {
            return;
        }
        this.status = TaskStatus.ARCHIVED;
        this.archivedAt = OffsetDateTime.now();
    }

    public void restore() {
        ensureStatusAllows("restore", TaskStatus.ARCHIVED);
        this.status = TaskStatus.PLANNED;
        this.archivedAt = null;
    }

    public void transitionTo(TaskStatus targetStatus) {
        if (targetStatus == null || targetStatus == status) {
            return;
        }
        switch (targetStatus) {
            case DRAFT -> {
                ensureStatusAllows("move to draft", TaskStatus.PLANNED);
                this.status = TaskStatus.DRAFT;
            }
            case PLANNED -> {
                if (status == TaskStatus.DRAFT) {
                    this.status = TaskStatus.PLANNED;
                    return;
                }
                if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED) {
                    reopen();
                    return;
                }
                if (status == TaskStatus.ARCHIVED) {
                    restore();
                    return;
                }
                ensureStatusAllows("move to planned", TaskStatus.SCHEDULED, TaskStatus.ON_HOLD);
                this.status = TaskStatus.PLANNED;
            }
            case SCHEDULED -> schedule(deadline, estimatedMinutes);
            case IN_PROGRESS -> {
                if (status == TaskStatus.ON_HOLD) {
                    resume();
                } else {
                    start();
                }
            }
            case ON_HOLD -> pause();
            case COMPLETED -> markAsCompleted();
            case CANCELLED -> cancel();
            case ARCHIVED -> archive();
        }
    }

    public void updateProgress(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Task progress must be between 0 and 100.");
        }
        this.progress = progress;
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    public boolean isOverdue(LocalDate today) {
        if (deadline == null || today == null) {
            return false;
        }
        return deadline.isBefore(today)
                && status != TaskStatus.COMPLETED
                && status != TaskStatus.CANCELLED;
    }

    public boolean belongsTo(UUID userId) {
        if (userId == null) {
            return false;
        }
        return Objects.equals(this.ownerId, userId);
    }

    public TaskTag assignTag(Tag tag) {
        for (TaskTag taskTag : taskTags) {
            if (taskTag.referencesTag(tag)) {
                return taskTag;
            }
        }

        TaskTag taskTag = TaskTag.attach(this, tag);
        taskTags.add(taskTag);
        tag.getTaskTags().add(taskTag);
        return taskTag;
    }

    public boolean removeTag(Tag tag) {
        if (tag == null) {
            return false;
        }

        TaskTag existingTaskTag = null;
        for (TaskTag taskTag : taskTags) {
            if (taskTag.referencesTag(tag)) {
                existingTaskTag = taskTag;
                break;
            }
        }
        if (existingTaskTag == null) {
            return false;
        }

        taskTags.remove(existingTaskTag);
        tag.getTaskTags().remove(existingTaskTag);
        return true;
    }

    public void setName(String name) {
        this.name = requireName(name);
    }

    public void setDescription(String description) {
        this.description = optionalText(description, "description", DESCRIPTION_MAX_LENGTH);
    }

    public void setProgress(Integer progress) {
        if (progress == null) {
            this.progress = 0;
            return;
        }
        updateProgress(progress);
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        if (estimatedMinutes != null && estimatedMinutes < 0) {
            throw new IllegalArgumentException("Task estimatedMinutes must not be negative.");
        }
        this.estimatedMinutes = estimatedMinutes;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        if (estimatedCost != null && estimatedCost.compareTo(ZERO_COST) < 0) {
            throw new IllegalArgumentException("Task estimatedCost must not be negative.");
        }
        this.estimatedCost = estimatedCost;
    }

    public void setScheduledWindow(
            OffsetDateTime scheduledStartAt,
            OffsetDateTime scheduledEndAt) {

        if (scheduledStartAt != null || scheduledEndAt != null) {
            requireChronologicalWindow(scheduledStartAt, scheduledEndAt, "scheduled");
        }
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
    }

    private void ensurePlanningEditable() {
        if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED || status == TaskStatus.ARCHIVED) {
            throw new IllegalStateException("Task planning cannot be edited in status " + status + ".");
        }
    }

    private void ensureStatusAllows(String action, TaskStatus... allowedStatuses) {
        for (TaskStatus allowedStatus : allowedStatuses) {
            if (status == allowedStatus) {
                return;
            }
        }
        throw new IllegalStateException("Task status " + status + " does not allow action " + action + ".");
    }

    private void requireChronologicalWindow(
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String fieldName) {

        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("Task " + fieldName + " window must have start before end.");
        }
    }

    private String requireName(String value) {
        String normalized = optionalText(value, "name", NAME_MAX_LENGTH);
        if (normalized == null) {
            throw new IllegalArgumentException("Task name is required.");
        }
        return normalized;
    }

    private String optionalText(String value, String fieldName, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = normalizeOptionalText(value);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Task " + fieldName + " must not exceed " + maxLength + " characters.");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Task that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
