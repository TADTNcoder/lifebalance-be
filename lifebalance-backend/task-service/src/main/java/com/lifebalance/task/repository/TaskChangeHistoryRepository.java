package com.lifebalance.task.repository;

import com.lifebalance.task.model.TaskChangeHistory;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TaskChangeHistoryRepository extends JpaRepository<TaskChangeHistory, UUID> {

    Page<TaskChangeHistory> findByOwnerIdOrderByOccurredAtDescIdDesc(UUID ownerId, Pageable pageable);

    @Query("""
            SELECT history
            FROM TaskChangeHistory history
            WHERE history.ownerId = :ownerId
              AND history.task.id = :taskId
            ORDER BY history.occurredAt DESC, history.id DESC
            """)
    Page<TaskChangeHistory> findByOwnerIdAndTaskId(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            Pageable pageable);

    Page<TaskChangeHistory> findByOwnerIdAndActionTypeOrderByOccurredAtDescIdDesc(
            UUID ownerId,
            TaskHistoryActionType actionType,
            Pageable pageable);
}
