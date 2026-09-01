package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

public interface EvaluationResultRepository
        extends JpaRepository<EvaluationResult, UUID>, JpaSpecificationExecutor<EvaluationResult> {

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

    /**
     * Search by generation timestamp using Criteria predicates only for filters
     * that are really present. This prevents nullable UUID/enum parameters from
     * becoming untyped "? IS NULL" binds on PostgreSQL.
     */
    default Page<EvaluationResult> search(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            EvaluationStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        Specification<EvaluationResult> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("ownerId"), ownerId));

            if (taskId != null) {
                predicates.add(criteriaBuilder.equal(root.get("taskId"), taskId));
            }
            if (capitalCycleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalCycleId"), capitalCycleId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("generatedAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("generatedAt"), to));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return findAll(specification, withGeneratedAtDefaultSort(pageable));
    }

    /**
     * Returns active evaluations whose evaluation period overlaps the requested
     * date range. Null filters are omitted from the Criteria query instead of
     * being bound into JPQL IS NULL expressions.
     */
    default Page<EvaluationResult> searchByEvaluationPeriod(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            EvaluationStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    ) {
        Specification<EvaluationResult> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("ownerId"), ownerId));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), EvaluationStatus.ARCHIVED));

            if (taskId != null) {
                predicates.add(criteriaBuilder.equal(root.get("taskId"), taskId));
            }
            if (capitalCycleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalCycleId"), capitalCycleId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (periodStart != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("periodEnd")),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("periodEnd"), periodStart)
                ));
            }
            if (periodEnd != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("periodStart")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("periodStart"), periodEnd)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return findAll(specification, withPeriodDefaultSort(pageable));
    }

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

    private static Pageable withGeneratedAtDefaultSort(Pageable pageable) {
        return withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "generatedAt", "id"));
    }

    private static Pageable withPeriodDefaultSort(Pageable pageable) {
        return withDefaultSort(
                pageable,
                Sort.by(Sort.Direction.DESC, "periodStart", "periodEnd", "generatedAt", "id")
        );
    }

    private static Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 10, defaultSort);
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }
}
