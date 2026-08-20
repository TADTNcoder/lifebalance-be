package com.lifebalance.task.validation;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.error.TaskErrorCode;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskLifecyclePolicyTest {

    private final TaskLifecyclePolicy policy = new TaskLifecyclePolicy();

    @Test
    void rejectsInvalidLifecycleTransitionBeforeStateMutation() {
        assertThatThrownBy(() -> policy.validateTransition(TaskStatus.DRAFT, TaskStatus.COMPLETED))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_INVALID_STATUS_TRANSITION);
    }

    @Test
    void rejectsPlanningEditForCompletedTask() {
        Task task = Task.builder()
                .ownerId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("Done task")
                .status(TaskStatus.COMPLETED)
                .build();

        assertThatThrownBy(() -> policy.validatePlanningEditable(task))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_PLANNING_LOCKED);
    }

    @Test
    void rejectsTimelineEligibilityWhenEstimatedTimeIsMissing() {
        Task task = Task.builder()
                .ownerId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("No estimate")
                .status(TaskStatus.PLANNED)
                .build();

        assertThatThrownBy(() -> policy.validateTimelineEligibility(task))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_TIMELINE_NOT_ELIGIBLE);
    }

    @Test
    void acceptsTimelineEligibilityForPlannedTaskWithEstimate() {
        Task task = Task.builder()
                .ownerId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("Estimated task")
                .status(TaskStatus.PLANNED)
                .estimatedMinutes(30)
                .build();

        policy.validateTimelineEligibility(task);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);
    }

    @Test
    void rejectsTimelineWindowWhenEndIsNotAfterStart() {
        OffsetDateTime now = OffsetDateTime.now();

        assertThatThrownBy(() -> policy.validateTimelineWindow(now, now))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_TIMELINE_INVALID_WINDOW);
    }
}
