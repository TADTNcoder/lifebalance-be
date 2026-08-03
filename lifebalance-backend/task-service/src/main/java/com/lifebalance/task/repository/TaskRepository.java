package com.lifebalance.task.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.task.model.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByTaskName(String taskName);
}
