package com.lifebalance.ai.repository;

import com.lifebalance.ai.domain.AiInsight;
import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightStatus;
import com.lifebalance.ai.domain.AiInsightType;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiInsightRepository extends JpaRepository<AiInsight, UUID> {

    Optional<AiInsight> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT insight
            FROM AiInsight insight
            WHERE insight.id = :insightId
              AND insight.ownerId = :ownerId
            """)
    Optional<AiInsight> findByIdAndOwnerIdForUpdate(
            @Param("insightId") UUID insightId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT insight
            FROM AiInsight insight
            WHERE insight.ownerId = :ownerId
              AND (:insightType IS NULL OR insight.insightType = :insightType)
              AND (:severity IS NULL OR insight.severity = :severity)
              AND (:status IS NULL OR insight.status = :status)
              AND (:referenceType IS NULL OR insight.referenceType = :referenceType)
              AND (:referenceId IS NULL OR insight.referenceId = :referenceId)
              AND (:periodStart IS NULL OR insight.periodEnd IS NULL OR insight.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR insight.periodStart IS NULL OR insight.periodStart <= :periodEnd)
            ORDER BY insight.generatedAt DESC, insight.id DESC
            """)
    Page<AiInsight> search(
            @Param("ownerId") UUID ownerId,
            @Param("insightType") AiInsightType insightType,
            @Param("severity") AiInsightSeverity severity,
            @Param("status") AiInsightStatus status,
            @Param("referenceType") String referenceType,
            @Param("referenceId") UUID referenceId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            Pageable pageable
    );
}
