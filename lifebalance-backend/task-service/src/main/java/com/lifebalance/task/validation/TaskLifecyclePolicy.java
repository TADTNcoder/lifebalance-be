package com.lifebalance.task.validation;

import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TaskLifecyclePolicy {

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TaskStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TaskStatus.DRAFT, EnumSet.of(
                TaskStatus.PLANNED,
                TaskStatus.SCHEDULED,
                TaskStatus.CANCELLED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.PLANNED, EnumSet.of(
                TaskStatus.DRAFT,
                TaskStatus.SCHEDULED,
                TaskStatus.IN_PROGRESS,
                TaskStatus.ON_HOLD,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.SCHEDULED, EnumSet.of(
                TaskStatus.PLANNED,
                TaskStatus.IN_PROGRESS,
                TaskStatus.ON_HOLD,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.IN_PROGRESS, EnumSet.of(
                TaskStatus.ON_HOLD,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.ON_HOLD, EnumSet.of(
                TaskStatus.PLANNED,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.COMPLETED, EnumSet.of(
                TaskStatus.PLANNED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.CANCELLED, EnumSet.of(
                TaskStatus.PLANNED,
                TaskStatus.ARCHIVED
        ));
        ALLOWED_TRANSITIONS.put(TaskStatus.ARCHIVED, EnumSet.of(TaskStatus.PLANNED));
    }

    private static final Set<TaskStatus> PLANNING_LOCKED_STATUSES = EnumSet.of(
            TaskStatus.COMPLETED,
            TaskStatus.CANCELLED,
            TaskStatus.ARCHIVED
    );

    private static final Set<TaskStatus> TIMELINE_ELIGIBLE_STATUSES = EnumSet.of(
            TaskStatus.DRAFT,
            TaskStatus.PLANNED,
            TaskStatus.SCHEDULED
    );

    public void validateTransition(
            TaskStatus sourceStatus,
            TaskStatus targetStatus) {

        if (targetStatus == null || targetStatus == sourceStatus) {
            return;
        }

        Set<TaskStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(
                sourceStatus,
                Set.of()
        );
        if (!allowedTargets.contains(targetStatus)) {
            throw TaskExceptions.invalidStatusTransition(sourceStatus, targetStatus);
        }
    }

    public void validatePlanningEditable(Task task) {
        if (task == null) {
            throw TaskExceptions.taskNotFound();
        }
        if (PLANNING_LOCKED_STATUSES.contains(task.getStatus())) {
            throw TaskExceptions.planningLocked(task.getStatus());
        }
    }

    public void validateTimelineEligibility(Task task) {
        if (task == null) {
            throw TaskExceptions.taskNotFound();
        }
        if (!TIMELINE_ELIGIBLE_STATUSES.contains(task.getStatus())) {
            throw TaskExceptions.timelineNotEligible("Task status " + task.getStatus() + " cannot be scheduled.");
        }
        if (task.getEstimatedMinutes() == null || task.getEstimatedMinutes() <= 0) {
            throw TaskExceptions.timelineNotEligible("Task estimatedMinutes must be greater than 0.");
        }
    }

    public void validateTimelineWindow(
            OffsetDateTime startAt,
            OffsetDateTime endAt) {

        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw TaskExceptions.invalidTimelineWindow();
        }
    }
}
