package com.lifebalance.task.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID> {

        Optional<Task> findByName(String name);

        Page<Task> findByNameContainingIgnoreCase(
                        String keyword,
                        Pageable pageable);

        Page<Task> findByStatus(
                        TaskStatus status,
                        Pageable pageable);

        Page<Task> findByPriority(
                        PriorityLevel priority,
                        Pageable pageable);

        Optional<Task> findByNameAndOwnerId(
                        String name,
                        UUID ownerId);

        Optional<Task> findByIdAndOwnerId(
                        UUID id,
                        UUID ownerId);

        boolean existsByNameAndOwnerId(
                        String name,
                        UUID ownerId);

        Page<Task> findByOwnerIdAndNameContainingIgnoreCase(
                        UUID ownerId,
                        String keyword,
                        Pageable pageable);
}