package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, UUID> {

    Optional<EvaluationResult> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT result
            FROM EvaluationResult result
            WHERE result.id = :evaluationId
              AND result.ownerId = :ownerId
            """)
    Optional<EvaluationResult> findByIdAndOwnerIdForUpdate(
            @Param("evaluationId") UUID evaluationId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT result
            FROM EvaluationResult result
            WHERE result.ownerId = :ownerId
              AND (:taskId IS NULL OR result.taskId = :taskId)
              AND (:capitalCycleId IS NULL OR result.capitalCycleId = :capitalCycleId)
              AND (:status IS NULL OR result.status = :status)
              AND (:from IS NULL OR result.generatedAt >= :from)
              AND (:to IS NULL OR result.generatedAt <= :to)
            ORDER BY result.generatedAt DESC, result.id DESC
            """)
    Page<EvaluationResult> search(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("status") EvaluationStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );

    @Query("""
            SELECT result
            FROM EvaluationResult result
            WHERE result.ownerId = :ownerId
              AND result.status <> com.lifebalance.analytics.domain.EvaluationStatus.ARCHIVED
              AND (:taskId IS NULL OR result.taskId = :taskId)
              AND (:capitalCycleId IS NULL OR result.capitalCycleId = :capitalCycleId)
              AND (:status IS NULL OR result.status = :status)
              AND (:periodStart IS NULL OR result.periodEnd IS NULL OR result.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR result.periodStart IS NULL OR result.periodStart <= :periodEnd)
            ORDER BY result.periodStart DESC, result.periodEnd DESC, result.generatedAt DESC, result.id DESC
            """)
    Page<EvaluationResult> searchByEvaluationPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("status") EvaluationStatus status,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            Pageable pageable
    );

    @Query("""
            SELECT result
            FROM EvaluationResult result
            WHERE result.ownerId = :ownerId
              AND result.status <> com.lifebalance.analytics.domain.EvaluationStatus.ARCHIVED
              AND (:periodStart IS NULL OR result.periodEnd IS NULL OR result.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR result.periodStart IS NULL OR result.periodStart <= :periodEnd)
            ORDER BY result.periodStart ASC, result.periodEnd ASC, result.generatedAt ASC, result.id ASC
            """)
    List<EvaluationResult> findActiveByOwnerAndPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("""
            SELECT COUNT(DISTINCT result.taskId)
            FROM EvaluationResult result
            WHERE result.ownerId = :ownerId
              AND result.status <> com.lifebalance.analytics.domain.EvaluationStatus.ARCHIVED
              AND (:periodStart IS NULL OR result.periodEnd IS NULL OR result.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR result.periodStart IS NULL OR result.periodStart <= :periodEnd)
            """)
    long countEvaluatedTasks(
            @Param("ownerId") UUID ownerId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("""
            SELECT result.efficiencyPercent
            FROM EvaluationResult result
            WHERE result.ownerId = :ownerId
              AND result.status <> com.lifebalance.analytics.domain.EvaluationStatus.ARCHIVED
              AND result.efficiencyPercent IS NOT NULL
              AND (:periodStart IS NULL OR result.periodEnd IS NULL OR result.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR result.periodStart IS NULL OR result.periodStart <= :periodEnd)
            """)
    List<BigDecimal> findEfficiencyPercentages(
            @Param("ownerId") UUID ownerId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("""
            SELECT COUNT(result)
            FROM EvaluationResult result
            WHERE result.ownerId = :ownerId
              AND result.status = :status
              AND (:periodStart IS NULL OR result.periodEnd IS NULL OR result.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR result.periodStart IS NULL OR result.periodStart <= :periodEnd)
            """)
    long countByOwnerStatusAndPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("status") EvaluationStatus status,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}
