package com.lifebalance.task.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_tags", schema = "task", indexes = @Index(name = "idx_task_tags_tag_task", columnList = "tag_id,task_id"))
public class TaskTag {

    @EmbeddedId
    private TaskTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static TaskTag attach(Task task, Tag tag) {
        requireAttachable(task, tag);
        return TaskTag.builder()
                .id(new TaskTagId(task.getId(), tag.getId()))
                .task(task)
                .tag(tag)
                .assignedAt(OffsetDateTime.now())
                .build();
    }

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    public boolean referencesTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        if (this.tag != null && Objects.equals(this.tag, tag)) {
            return true;
        }
        return id != null && Objects.equals(id.getTagId(), tag.getId());
    }

    private static void requireAttachable(Task task, Tag tag) {
        if (task == null) {
            throw new IllegalArgumentException("Task is required before assigning a tag.");
        }
        if (tag == null) {
            throw new IllegalArgumentException("Tag is required before assigning it to a task.");
        }
        if (task.getId() == null) {
            throw new IllegalStateException("Task must be persisted before assigning a tag.");
        }
        if (tag.getId() == null) {
            throw new IllegalStateException("Tag must be persisted before assigning it to a task.");
        }
        if (!Objects.equals(task.getUserId(), tag.getUserId())) {
            throw new IllegalArgumentException("Tag must belong to the same user as the task.");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TaskTag that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
