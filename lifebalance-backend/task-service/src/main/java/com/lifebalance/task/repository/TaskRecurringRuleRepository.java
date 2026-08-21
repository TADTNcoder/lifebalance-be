package com.lifebalance.task.repository;

import com.lifebalance.task.model.TaskRecurringRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TaskRecurringRuleRepository extends JpaRepository<TaskRecurringRule, UUID> {

    Optional<TaskRecurringRule> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            SELECT rule
            FROM TaskRecurringRule rule
            WHERE rule.ownerId = :ownerId
              AND rule.task.id = :taskId
            ORDER BY rule.createdAt DESC, rule.id DESC
            """)
    Page<TaskRecurringRule> findByOwnerIdAndTaskIdOrderByCreatedAtDesc(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            Pageable pageable);
}
