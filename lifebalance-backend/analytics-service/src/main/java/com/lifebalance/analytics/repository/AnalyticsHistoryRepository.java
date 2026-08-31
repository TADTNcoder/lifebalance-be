package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.AnalyticsHistory;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsHistoryRepository extends JpaRepository<AnalyticsHistory, UUID> {

    @Query("""
            SELECT history
            FROM AnalyticsHistory history
            WHERE history.ownerId = :ownerId
              AND (:actionType IS NULL OR history.actionType = :actionType)
              AND (:actualRecordId IS NULL OR history.actualRecord.id = :actualRecordId)
              AND (:evaluationResultId IS NULL OR history.evaluationResult.id = :evaluationResultId)
              AND (:reportId IS NULL OR history.report.id = :reportId)
              AND history.occurredAt >= :from
              AND history.occurredAt <= :to
            ORDER BY history.occurredAt DESC, history.id DESC
            """)
    Page<AnalyticsHistory> search(
            @Param("ownerId") UUID ownerId,
            @Param("actionType") AnalyticsHistoryActionType actionType,
            @Param("actualRecordId") UUID actualRecordId,
            @Param("evaluationResultId") UUID evaluationResultId,
            @Param("reportId") UUID reportId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}
