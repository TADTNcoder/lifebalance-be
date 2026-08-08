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
        task.cancel();
        task.reopen();
        task.archive();
        task.restore();

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
        task.plan(PriorityLevel.MEDIUM, LocalDate.now().plusDays(1), null, null, null);
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
        task.updateDetails("Changed", null, PriorityLevel.LOW, null, null, null, null);

        assertThat(task.getName()).isEqualTo("Changed");
    }

    @Test
    void overdueRequiresPastDeadlineAndOpenStatus() {
        LocalDate today = LocalDate.of(2026, 8, 8);
        Task task = baseTask();
        task.setDeadline(today.minusDays(1));

        assertThat(task.isOverdue(today)).isTrue();

        task.plan(PriorityLevel.LOW, today.minusDays(1), null, null, null);
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
                .userId(userId)
                .name(" ")
                .status(TaskStatus.DRAFT)
                .progress(101)
                .estimatedMinutes(-1)
                .estimatedCost(BigDecimal.valueOf(-1))
                .build();

        assertThat(validator.validate(task))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "progress", "estimatedMinutes", "estimatedCost");
    }

    private Task baseTask() {
        return Task.builder()
                .userId(userId)
                .name("Morning planning")
                .build();
    }
}
