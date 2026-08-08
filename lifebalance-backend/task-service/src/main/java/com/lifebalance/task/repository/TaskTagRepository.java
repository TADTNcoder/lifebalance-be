package com.lifebalance.task.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.task.model.TaskTag;
import com.lifebalance.task.model.TaskTagId;

public interface TaskTagRepository
        extends JpaRepository<TaskTag, TaskTagId> {

    boolean existsByTaskIdAndTagId(
            UUID taskId,
            UUID tagId);

    void deleteByTaskIdAndTagId(
            UUID taskId,
            UUID tagId);

    List<TaskTag> findByTaskId(UUID taskId);
}