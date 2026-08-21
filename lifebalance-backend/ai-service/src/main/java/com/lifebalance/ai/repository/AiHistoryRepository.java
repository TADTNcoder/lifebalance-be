package com.lifebalance.ai.repository;

import com.lifebalance.ai.domain.AiHistory;
import com.lifebalance.ai.domain.AiHistoryActionType;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiHistoryRepository extends JpaRepository<AiHistory, UUID> {

    @Query("""
            SELECT history
            FROM AiHistory history
            WHERE history.ownerId = :ownerId
              AND (:actionType IS NULL OR history.actionType = :actionType)
              AND (:conversationId IS NULL OR history.conversation.id = :conversationId)
              AND (:recommendationId IS NULL OR history.recommendation.id = :recommendationId)
              AND (:insightId IS NULL OR history.insight.id = :insightId)
              AND (:from IS NULL OR history.occurredAt >= :from)
              AND (:to IS NULL OR history.occurredAt <= :to)
            ORDER BY history.occurredAt DESC, history.id DESC
            """)
    Page<AiHistory> search(
            @Param("ownerId") UUID ownerId,
            @Param("actionType") AiHistoryActionType actionType,
            @Param("conversationId") UUID conversationId,
            @Param("recommendationId") UUID recommendationId,
            @Param("insightId") UUID insightId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}
