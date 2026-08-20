package com.lifebalance.task.model;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    private final UUID userId = UUID.randomUUID();

    @Test
    void newTaskDefaultsToDraftWithZeroProgress() {
        Task task = baseTask();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(task.getProgress()).isZero();
        assertThat(task.belongsTo(userId)).isTrue();
    }

    @Test
    void belongsToChecksBothOwnerAndUserId() {
        UUID otherUserId = UUID.randomUUID();
        Task task = baseTask();

        assertThat(task.belongsTo(userId)).isTrue();
        assertThat(task.belongsTo(otherUserId)).isFalse();
        assertThat(task.belongsTo(null)).isFalse();
    }

    @Test
    void updateProgressAcceptsOnlyZeroToOneHundred() {
        Task task = baseTask();

        task.updateProgress(50);

        assertThat(task.getProgress()).isEqualTo(50);
        assertThatThrownBy(() -> task.updateProgress(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> task.updateProgress(101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void scheduleRequiresPositiveEstimatedMinutes() {
        Task task = baseTask();
        LocalDate deadline = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> task.schedule(deadline, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> task.schedule(deadline, 0))
                .isInstanceOf(IllegalStateException.class);

        task.schedule(deadline, 30);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.SCHEDULED);
        assertThat(task.getEstimatedMinutes()).isEqualTo(30);
    }

    @Test
    void planningAttributesAcceptNullOrPositiveValues() {
        Task task = baseTask();

        task.setEstimatedMinutes(null);
        assertThat(task.getEstimatedMinutes()).isNull();

        task.setEstimatedMinutes(0);
        assertThat(task.getEstimatedMinutes()).isZero();

        task.setEstimatedMinutes(60);
        assertThat(task.getEstimatedMinutes()).isEqualTo(60);

        assertThatThrownBy(() -> task.setEstimatedMinutes(-5))
                .isInstanceOf(IllegalArgumentException.class);

        task.setEstimatedCost(null);
        assertThat(task.getEstimatedCost()).isNull();

        task.setEstimatedCost(BigDecimal.ZERO);
        assertThat(task.getEstimatedCost()).isEqualByComparingTo(BigDecimal.ZERO);

        task.setEstimatedCost(BigDecimal.valueOf(100.50));
        assertThat(task.getEstimatedCost()).isEqualByComparingTo("100.50");

        assertThatThrownBy(() -> task.setEstimatedCost(BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lifecycleSupportsPauseResumeCompleteCancelReopenArchiveRestore() {
        Task task = baseTask();

        task.plan(PriorityLevel.HIGH, LocalDate.now().plusDays(1), 45, BigDecimal.TEN, null);
        task.start();
        task.pause();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ON_HOLD);

        task.resume();
        task.markAsCompleted();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        task.reopen();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);

        task.cancel();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);

        task.reopen();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);

        task.archive();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ARCHIVED);

        task.restore();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);
    }

    @Test
    void transitionToCoversAllLifecycleStates() {
        Task task = baseTask();

        // DRAFT -> PLANNED
        task.transitionTo(TaskStatus.PLANNED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);

        // PLANNED -> DRAFT
        task.transitionTo(TaskStatus.DRAFT);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DRAFT);

        // DRAFT -> SCHEDULED
        task.setDeadline(LocalDate.now().plusDays(2));
        task.setEstimatedMinutes(60);
        task.transitionTo(TaskStatus.SCHEDULED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.SCHEDULED);

        // SCHEDULED -> IN_PROGRESS
        task.transitionTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        // IN_PROGRESS -> ON_HOLD
        task.transitionTo(TaskStatus.ON_HOLD);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ON_HOLD);

        // ON_HOLD -> IN_PROGRESS
        task.transitionTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        // IN_PROGRESS -> COMPLETED
        task.transitionTo(TaskStatus.COMPLETED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        // COMPLETED -> ARCHIVED
        task.transitionTo(TaskStatus.ARCHIVED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ARCHIVED);

        // ARCHIVED -> PLANNED
        task.transitionTo(TaskStatus.PLANNED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);

        // PLANNED -> CANCELLED
        task.transitionTo(TaskStatus.CANCELLED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);

        // CANCELLED -> PLANNED
        task.transitionTo(TaskStatus.PLANNED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);
    }

    @Test
    void draftCannotBeCompletedDirectly() {
        Task task = baseTask();

        assertThatThrownBy(task::markAsCompleted)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completedTaskCannotEditPlanningBeforeReopen() {
        Task task = baseTask();
        task.plan(PriorityLevel.MEDIUM, LocalDate.now().plusDays(1), 30, null, null);
        task.markAsCompleted();

        assertThatThrownBy(() -> task.updateDetails(
                "Changed",
                null,
                PriorityLevel.LOW,
                null,
                null,
                null,
                null
        )).isInstanceOf(IllegalStateException.class);

        task.reopen();
        task.updateDetails("Changed", null, PriorityLevel.LOW, null, 30, null, null);

        assertThat(task.getName()).isEqualTo("Changed");
    }

    @Test
    void overdueRequiresPastDeadlineAndOpenStatus() {
        LocalDate today = LocalDate.of(2026, 8, 8);
        Task task = baseTask();
        task.setDeadline(today.minusDays(1));

        assertThat(task.isOverdue(today)).isTrue();

        task.plan(PriorityLevel.LOW, today.minusDays(1), 30, null, null);
        task.markAsCompleted();

        assertThat(task.isOverdue(today)).isFalse();
    }

    @Test
    void equalsUsesPersistedIdentityOnly() {
        UUID id = UUID.randomUUID();
        Task first = baseTask();
        Task second = baseTask();
        first.setId(id);
        second.setId(id);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(baseTask()).isNotEqualTo(baseTask());
    }

    @Test
    void beanValidationCatchesRequiredAndRangeConstraints() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Task task = Task.builder()
                .name(" ")
                .status(TaskStatus.DRAFT)
                .progress(101)
                .estimatedMinutes(-1)
                .estimatedCost(BigDecimal.valueOf(-1))
                .build();

        assertThat(validator.validate(task))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("ownerId", "userId", "name", "progress", "estimatedMinutes", "estimatedCost");
    }

    @Test
    void taskNameCannotBeBlankOrExceedMaxLength() {
        Task task = baseTask();

        assertThatThrownBy(() -> task.setName("   "))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> task.setName("a".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class);

        task.setName("Valid task name");
        assertThat(task.getName()).isEqualTo("Valid task name");
    }

    @Test
    void taskDescriptionCannotExceedMaxLength() {
        Task task = baseTask();

        assertThatThrownBy(() -> task.setDescription("d".repeat(2001)))
                .isInstanceOf(IllegalArgumentException.class);

        task.setDescription("Valid description");
        assertThat(task.getDescription()).isEqualTo("Valid description");
    }

    private Task baseTask() {
        return Task.builder()
                .ownerId(userId)
                .userId(userId)
                .name("Morning planning")
                .build();
    }
}
