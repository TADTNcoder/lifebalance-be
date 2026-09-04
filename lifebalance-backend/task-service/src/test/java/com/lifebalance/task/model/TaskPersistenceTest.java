package com.lifebalance.task.model;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:task_mapping;MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS task",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void persistsTaskCoreMappingWithCategoryRelation() {
        Category category = Category.builder()
                .name("Health")
                .description("Health tasks")
                .build();
        entityManager.persist(category);

        UUID userId = UUID.randomUUID();
        Task task = Task.builder()
                .ownerId(userId)
                .userId(userId)
                .name("Exercise")
                .description("Strength training")
                .priority(PriorityLevel.CRITICAL)
                .deadline(LocalDate.of(2026, 8, 9))
                .progress(25)
                .estimatedMinutes(60)
                .estimatedCost(BigDecimal.valueOf(15.50))
                .category(category)
                .build();

        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        Task persisted = entityManager.find(Task.class, task.getId());

        assertThat(persisted.getOwnerId()).isEqualTo(userId);
        assertThat(persisted.getUserId()).isEqualTo(userId);
        assertThat(persisted.getName()).isEqualTo("Exercise");
        assertThat(persisted.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(persisted.getPriority()).isEqualTo(PriorityLevel.CRITICAL);
        assertThat(persisted.getProgress()).isEqualTo(25);
        assertThat(persisted.getEstimatedMinutes()).isEqualTo(60);
        assertThat(persisted.getEstimatedCost()).isEqualByComparingTo("15.5000");
        assertThat(persisted.getCategory().getName()).isEqualTo("Health");
    }

    @Test
    void taskNameLookupUsesTrimmedCaseInsensitiveOwnerScope() {
        UUID ownerId = UUID.randomUUID();
        Task task = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name("  Deep Work  ")
                .progress(0)
                .build();

        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        assertThat(taskRepository.findByNameAndOwnerId("deep work", ownerId))
                .isPresent()
                .get()
                .extracting(Task::getName)
                .isEqualTo("Deep Work");
    }

    @Test
    void locksEveryTaskInTheMonthlyIncomeGroupForLifecycleSerialization() {
        UUID ownerId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Task first = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Salary occurrence 1")
                .monthlyIncomeGroupId(groupId)
                .progress(0)
                .build();
        Task second = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Salary occurrence 2")
                .monthlyIncomeGroupId(groupId)
                .progress(0)
                .build();
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();
        entityManager.clear();

        assertThat(taskRepository.lockMonthlyIncomeGroupForTask(first.getId(), ownerId))
                .extracting(Task::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void searchesTasksByDeadlineRange() {
        UUID ownerId = UUID.randomUUID();
        Task beforeRange = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Before range")
                .deadline(LocalDate.of(2026, 8, 29))
                .progress(0)
                .build();
        Task inRange = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name("In range")
                .deadline(LocalDate.of(2026, 8, 30))
                .progress(0)
                .build();
        Task withoutDeadline = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Without deadline")
                .progress(0)
                .build();

        entityManager.persist(beforeRange);
        entityManager.persist(inRange);
        entityManager.persist(withoutDeadline);
        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.searchByOwnerAndFilters(
                ownerId,
                "",
                null,
                null,
                null,
                LocalDate.of(2026, 8, 30),
                LocalDate.of(2026, 8, 31),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Task::getName)
                .containsExactly("In range");
    }

    @Test
    void adminSearchReturnsMatchingTasksFromEveryOwner() {
        UUID firstOwnerId = UUID.randomUUID();
        UUID secondOwnerId = UUID.randomUUID();
        Task firstOwnerTask = Task.builder()
                .ownerId(firstOwnerId)
                .userId(firstOwnerId)
                .name("Shared quarterly report")
                .status(TaskStatus.PLANNED)
                .priority(PriorityLevel.HIGH)
                .deadline(LocalDate.of(2026, 9, 10))
                .progress(0)
                .build();
        Task secondOwnerTask = Task.builder()
                .ownerId(secondOwnerId)
                .userId(secondOwnerId)
                .name("Shared annual report")
                .status(TaskStatus.PLANNED)
                .priority(PriorityLevel.HIGH)
                .deadline(LocalDate.of(2026, 9, 20))
                .progress(0)
                .build();
        Task excludedTask = Task.builder()
                .ownerId(secondOwnerId)
                .userId(secondOwnerId)
                .name("Private draft")
                .status(TaskStatus.DRAFT)
                .priority(PriorityLevel.LOW)
                .deadline(LocalDate.of(2026, 9, 20))
                .progress(0)
                .build();

        entityManager.persist(firstOwnerTask);
        entityManager.persist(secondOwnerTask);
        entityManager.persist(excludedTask);
        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.searchAllByFilters(
                "report",
                TaskStatus.PLANNED,
                PriorityLevel.HIGH,
                null,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Task::getOwnerId)
                .containsExactlyInAnyOrder(firstOwnerId, secondOwnerId);
    }

    @Test
    void validationRunsBeforePersistingInvalidTask() {
        Task task = Task.builder()
                .ownerId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("")
                .progress(0)
                .build();

        assertThatThrownBy(() -> {
            entityManager.persist(task);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }
}
