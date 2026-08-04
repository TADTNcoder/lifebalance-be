package com.lifebalance.task.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.task.model.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByTaskName(String taskName);
}
