package com.lifebalance.timeline.repository;

import com.lifebalance.timeline.domain.TimelineHistory;
import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimelineHistoryRepository extends JpaRepository<TimelineHistory, UUID> {

    @EntityGraph(attributePaths = {"placement", "task"})
    @Query("""
            SELECT history
            FROM TimelineHistory history
            WHERE history.ownerId = :ownerId
              AND (:placementId IS NULL OR history.placement.id = :placementId)
              AND (:taskId IS NULL OR history.task.id = :taskId)
              AND (:actionType IS NULL OR history.actionType = :actionType)
            ORDER BY history.occurredAt DESC, history.id DESC
            """)
    Page<TimelineHistory> search(
            @Param("ownerId") UUID ownerId,
            @Param("placementId") UUID placementId,
            @Param("taskId") UUID taskId,
            @Param("actionType") TimelineHistoryActionType actionType,
            Pageable pageable
    );
}
