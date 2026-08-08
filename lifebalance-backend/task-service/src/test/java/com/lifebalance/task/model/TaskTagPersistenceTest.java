package com.lifebalance.task.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:task_tag_mapping;MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS task",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskTagPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsTaskTagCompositeKeyAndRelationships() {
        UUID userId = UUID.randomUUID();
        Task task = persistTask(userId, "Exercise");
        Tag tag = persistTag(userId, "Health");
        entityManager.flush();

        TaskTag taskTag = TaskTag.attach(task, tag);
        entityManager.persist(taskTag);
        entityManager.flush();
        entityManager.clear();

        TaskTag persisted = entityManager.find(TaskTag.class, new TaskTagId(task.getId(), tag.getId()));

        assertThat(persisted).isNotNull();
        assertThat(persisted.getId()).isEqualTo(new TaskTagId(task.getId(), tag.getId()));
        assertThat(persisted.getTask().getName()).isEqualTo("Exercise");
        assertThat(persisted.getTag().getName()).isEqualTo("Health");
        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    @Test
    void mapsTaskAndTagCollectionsBidirectionally() {
        UUID userId = UUID.randomUUID();
        Task task = persistTask(userId, "Read");
        Tag tag = persistTag(userId, "Study");
        entityManager.flush();

        entityManager.persist(TaskTag.attach(task, tag));
        entityManager.flush();
        entityManager.clear();

        Task foundTask = entityManager.find(Task.class, task.getId());
        Tag foundTag = entityManager.find(Tag.class, tag.getId());

        assertThat(foundTask.getTaskTags())
                .hasSize(1)
                .first()
                .satisfies(found -> assertThat(found.getTag().getName()).isEqualTo("Study"));
        assertThat(foundTag.getTaskTags())
                .hasSize(1)
                .first()
                .satisfies(found -> assertThat(found.getTask().getName()).isEqualTo("Read"));
    }

    @Test
    void removingTaskTagDoesNotRemoveTaskOrTag() {
        UUID userId = UUID.randomUUID();
        Task task = persistTask(userId, "Budget");
        Tag tag = persistTag(userId, "Finance");
        entityManager.flush();

        TaskTag taskTag = TaskTag.attach(task, tag);
        entityManager.persist(taskTag);
        entityManager.flush();

        entityManager.remove(taskTag);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(TaskTag.class, new TaskTagId(task.getId(), tag.getId()))).isNull();
        assertThat(entityManager.find(Task.class, task.getId())).isNotNull();
        assertThat(entityManager.find(Tag.class, tag.getId())).isNotNull();
    }

    private Task persistTask(UUID userId, String name) {
        Task task = Task.builder()
                .userId(userId)
                .name(name)
                .build();
        entityManager.persist(task);
        return task;
    }

    private Tag persistTag(UUID userId, String name) {
        Tag tag = Tag.builder()
                .userId(userId)
                .name(name)
                .build();
        entityManager.persist(tag);
        return tag;
    }
}
