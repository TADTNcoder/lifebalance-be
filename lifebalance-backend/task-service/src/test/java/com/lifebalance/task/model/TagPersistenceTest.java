package com.lifebalance.task.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:tag_mapping;MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS task",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TagPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsTagCoreMapping() {
        UUID userId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .userId(userId)
                .name("Health")
                .build();

        entityManager.persist(tag);
        entityManager.flush();
        entityManager.clear();

        Tag persisted = entityManager.find(Tag.class, tag.getId());

        assertThat(persisted.getUserId()).isEqualTo(userId);
        assertThat(persisted.getName()).isEqualTo("Health");
        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    @Test
    void validationRunsBeforePersistingInvalidTag() {
        Tag tag = Tag.builder()
                .userId(UUID.randomUUID())
                .name("")
                .build();

        assertThatThrownBy(() -> {
            entityManager.persist(tag);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void softDeleteFiltersRemovedTag() {
        Tag tag = Tag.builder()
                .userId(UUID.randomUUID())
                .name("Work")
                .build();
        entityManager.persist(tag);
        entityManager.flush();
        UUID tagId = tag.getId();

        entityManager.remove(tag);
        entityManager.flush();
        entityManager.clear();

        Tag removed = entityManager.find(Tag.class, tagId);
        Object deletedAt = entityManager
                .createNativeQuery("SELECT deleted_at FROM task.tags WHERE id = ?")
                .setParameter(1, tagId)
                .getSingleResult();

        assertThat(removed).isNull();
        assertThat(deletedAt).isInstanceOf(OffsetDateTime.class);
    }
}
