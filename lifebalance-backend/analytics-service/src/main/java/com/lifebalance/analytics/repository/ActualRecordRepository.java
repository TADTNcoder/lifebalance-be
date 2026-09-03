package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
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

public interface ActualRecordRepository
        extends JpaRepository<ActualRecord, UUID>,
        JpaSpecificationExecutor<ActualRecord>,
        ActualRecordAggregateRepository {

    Optional<ActualRecord> findByIdAndOwnerId(UUID id, UUID ownerId);

    long countByOwnerIdAndTaskIdAndStatus(UUID ownerId, UUID taskId, ActualRecordStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT record
            FROM ActualRecord record
            WHERE record.id = :actualRecordId
              AND record.ownerId = :ownerId
            """)
    Optional<ActualRecord> findByIdAndOwnerIdForUpdate(
            @Param("actualRecordId") UUID actualRecordId,
            @Param("ownerId") UUID ownerId
    );

    /**
     * Dynamic search instead of the JPQL pattern
     * (:param IS NULL OR field = :param).
     *
     * PostgreSQL/Hibernate can fail to infer the SQL type of a null bind parameter
     * used in an IS NULL expression. Building predicates only for filters that are
     * actually present avoids those untyped null parameters completely.
     */
    default Page<ActualRecord> search(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            UUID categoryId,
            ActualRecordType recordType,
            ActualRecordStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        Specification<ActualRecord> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("ownerId"), ownerId));

            if (taskId != null) {
                predicates.add(criteriaBuilder.equal(root.get("taskId"), taskId));
            }
            if (capitalCycleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalCycleId"), capitalCycleId));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }
            if (recordType != null) {
                predicates.add(criteriaBuilder.equal(root.get("recordType"), recordType));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("actualDate"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("actualDate"), to));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return findAll(specification, withDefaultSearchSort(pageable));
    }

    @Query("""
            SELECT COUNT(record)
            FROM ActualRecord record
            WHERE record.ownerId = :ownerId
              AND record.status = com.lifebalance.analytics.domain.ActualRecordStatus.ACTIVE
              AND (:from IS NULL OR record.actualDate >= :from)
              AND (:to IS NULL OR record.actualDate <= :to)
            """)
    long countActiveRecords(
            @Param("ownerId") UUID ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT COUNT(DISTINCT record.taskId)
            FROM ActualRecord record
            WHERE record.ownerId = :ownerId
              AND record.status = com.lifebalance.analytics.domain.ActualRecordStatus.ACTIVE
              AND (:from IS NULL OR record.actualDate >= :from)
              AND (:to IS NULL OR record.actualDate <= :to)
            """)
    long countDistinctActiveTasks(
            @Param("ownerId") UUID ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    private static Pageable withDefaultSearchSort(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.DESC, "actualDate", "createdAt", "id")
            );
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "actualDate", "createdAt", "id")
        );
    }
}
