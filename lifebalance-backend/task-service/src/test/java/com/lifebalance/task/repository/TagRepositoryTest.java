package com.lifebalance.task.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.lifebalance.task.model.Tag;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskTag;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:tag_repository;MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS task",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsTagsByUserOrderedByNameAndExcludesOtherUsers() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        persistTag(userId, "Work");
        persistTag(userId, "Health");
        persistTag(otherUserId, "Finance");
        entityManager.flush();
        entityManager.clear();

        List<Tag> tags = tagRepository.findByUserIdOrderByNameAsc(userId);

        assertThat(tags)
                .extracting(Tag::getName)
                .containsExactly("Health", "Work");
    }

    @Test
    void findsAndChecksTagByIdWithinOwnerScope() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Tag tag = persistTag(userId, "Focus");
        entityManager.flush();
        entityManager.clear();

        assertThat(tagRepository.findByIdAndUserId(tag.getId(), userId))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("Focus");
        assertThat(tagRepository.findByIdAndUserId(tag.getId(), otherUserId)).isEmpty();
        assertThat(tagRepository.existsByIdAndUserId(tag.getId(), userId)).isTrue();
        assertThat(tagRepository.existsByIdAndUserId(tag.getId(), otherUserId)).isFalse();
    }

    @Test
    void findsAndChecksTagNameCaseInsensitivelyWithinOwnerScope() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        persistTag(userId, " Work ");
        persistTag(otherUserId, "Work");
        entityManager.flush();
        entityManager.clear();

        assertThat(tagRepository.findByUserIdAndName(userId, "work"))
                .isPresent()
                .get()
                .extracting(Tag::getUserId)
                .isEqualTo(userId);
        assertThat(tagRepository.existsByUserIdAndName(userId, " WORK ")).isTrue();
        assertThat(tagRepository.findByUserIdAndName(userId, "Finance")).isEmpty();
    }

    @Test
    void findsAllRequestedTagsWithinOwnerScope() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Tag first = persistTag(userId, "Health");
        Tag second = persistTag(userId, "Work");
        Tag otherUserTag = persistTag(otherUserId, "Finance");
        entityManager.flush();
        entityManager.clear();

        List<Tag> tags = tagRepository.findAllByIdInAndUserId(
                List.of(first.getId(), second.getId(), otherUserTag.getId()),
                userId
        );

        assertThat(tags)
                .extracting(Tag::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void findsTaskTagsOnlyWhenTaskAndTagsBelongToUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Task task = persistTask(userId, "Plan sprint");
        Task otherUserTask = persistTask(otherUserId, "Other task");
        Tag work = persistTag(userId, "Work");
        Tag health = persistTag(userId, "Health");
        Tag otherUserTag = persistTag(otherUserId, "Private");
        entityManager.flush();

        entityManager.persist(TaskTag.attach(task, work));
        entityManager.persist(TaskTag.attach(task, health));
        entityManager.persist(TaskTag.attach(otherUserTask, otherUserTag));
        entityManager.flush();
        entityManager.clear();

        List<Tag> tags = tagRepository.findByTaskIdAndUserId(task.getId(), userId);
        List<Tag> otherScopedTags = tagRepository.findByTaskIdAndUserId(task.getId(), otherUserId);

        assertThat(tags)
                .extracting(Tag::getName)
                .containsExactly("Health", "Work");
        assertThat(otherScopedTags).isEmpty();
    }

    @Test
    void repositoryQueriesExcludeSoftDeletedTags() {
        UUID userId = UUID.randomUUID();
        Tag activeTag = persistTag(userId, "Active");
        Tag deletedTag = persistTag(userId, "Deleted");
        entityManager.flush();

        entityManager.remove(deletedTag);
        entityManager.flush();
        entityManager.clear();

        assertThat(tagRepository.findByUserIdOrderByNameAsc(userId))
                .extracting(Tag::getId)
                .containsExactly(activeTag.getId());
        assertThat(tagRepository.findByIdAndUserId(deletedTag.getId(), userId)).isEmpty();
        assertThat(tagRepository.existsByIdAndUserId(deletedTag.getId(), userId)).isFalse();
        assertThat(tagRepository.findAllByIdInAndUserId(
                List.of(activeTag.getId(), deletedTag.getId()),
                userId
        )).extracting(Tag::getId)
                .containsExactly(activeTag.getId());
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
