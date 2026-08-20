package com.lifebalance.task.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID> {

        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND lower(trim(task.name)) = lower(trim(:name))
                        """)
        Optional<Task> findByNameAndOwnerId(
                        @Param("name") String name,
                        @Param("ownerId") UUID ownerId);

        Optional<Task> findByIdAndOwnerId(
                        UUID id,
                        UUID ownerId);

        @Query("""
                        SELECT CASE WHEN COUNT(task) > 0 THEN true ELSE false END
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND lower(trim(task.name)) = lower(trim(:name))
                        """)
        boolean existsByNameAndOwnerId(
                        @Param("name") String name,
                        @Param("ownerId") UUID ownerId);

        Page<Task> findByOwnerIdAndNameContainingIgnoreCase(
                        UUID ownerId,
                        String keyword,
                        Pageable pageable);

        Page<Task> findByOwnerIdAndStatus(
                        UUID ownerId,
                        TaskStatus status,
                        Pageable pageable);

        Page<Task> findByOwnerIdAndPriority(
                        UUID ownerId,
                        PriorityLevel priority,
                        Pageable pageable);
}
