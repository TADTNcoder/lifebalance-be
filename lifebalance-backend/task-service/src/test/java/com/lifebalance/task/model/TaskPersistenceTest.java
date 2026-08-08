package com.lifebalance.task.model;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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

    @Test
    void persistsTaskCoreMappingWithCategoryRelation() {
        Category category = Category.builder()
                .name("Health")
                .description("Health tasks")
                .build();
        entityManager.persist(category);

        UUID userId = UUID.randomUUID();
        Task task = Task.builder()
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
    void validationRunsBeforePersistingInvalidTask() {
        Task task = Task.builder()
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
