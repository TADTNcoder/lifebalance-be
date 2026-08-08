package com.lifebalance.task.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TaskTagTest {

    @Test
    void taskTagIdUsesTaskAndTagIdsForEquality() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        TaskTagId first = new TaskTagId(taskId, tagId);
        TaskTagId second = new TaskTagId(taskId, tagId);
        TaskTagId differentTag = new TaskTagId(taskId, UUID.randomUUID());

        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second)
                .isNotEqualTo(differentTag);
    }

    @Test
    void attachCreatesCompositeIdFromTaskAndTagIds() {
        UUID userId = UUID.randomUUID();
        Task task = persistedTask(userId);
        Tag tag = persistedTag(userId);

        TaskTag taskTag = TaskTag.attach(task, tag);

        assertThat(taskTag.getId()).isEqualTo(new TaskTagId(task.getId(), tag.getId()));
        assertThat(taskTag.getTask()).isSameAs(task);
        assertThat(taskTag.getTag()).isSameAs(tag);
    }

    @Test
    void attachRejectsCrossOwnerTag() {
        Task task = persistedTask(UUID.randomUUID());
        Tag tag = persistedTag(UUID.randomUUID());

        assertThatThrownBy(() -> TaskTag.attach(task, tag))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same user");
    }

    @Test
    void assignTagKeepsSingleAssociationForDuplicateTag() {
        UUID userId = UUID.randomUUID();
        Task task = persistedTask(userId);
        Tag tag = persistedTag(userId);

        TaskTag first = task.assignTag(tag);
        TaskTag second = task.assignTag(tag);

        assertThat(second).isSameAs(first);
        assertThat(task.getTaskTags()).containsExactly(first);
        assertThat(tag.getTaskTags()).containsExactly(first);
    }

    @Test
    void removeTagDetachesOnlyAssociation() {
        UUID userId = UUID.randomUUID();
        Task task = persistedTask(userId);
        Tag tag = persistedTag(userId);
        TaskTag taskTag = task.assignTag(tag);

        boolean removed = task.removeTag(tag);
        boolean removedAgain = task.removeTag(tag);

        assertThat(removed).isTrue();
        assertThat(removedAgain).isFalse();
        assertThat(task.getTaskTags()).doesNotContain(taskTag);
        assertThat(tag.getTaskTags()).doesNotContain(taskTag);
    }

    private Task persistedTask(UUID userId) {
        return Task.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Planning")
                .build();
    }

    private Tag persistedTag(UUID userId) {
        return Tag.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Work")
                .build();
    }
}
