package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActualRecordRepository extends JpaRepository<ActualRecord, UUID> {

    Optional<ActualRecord> findByIdAndOwnerId(UUID id, UUID ownerId);

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

    @Query("""
            SELECT record
            FROM ActualRecord record
            WHERE record.ownerId = :ownerId
              AND (:taskId IS NULL OR record.taskId = :taskId)
              AND (:capitalCycleId IS NULL OR record.capitalCycleId = :capitalCycleId)
              AND (:categoryId IS NULL OR record.categoryId = :categoryId)
              AND (:recordType IS NULL OR record.recordType = :recordType)
              AND (:status IS NULL OR record.status = :status)
              AND (:from IS NULL OR record.actualDate >= :from)
              AND (:to IS NULL OR record.actualDate <= :to)
            ORDER BY record.actualDate DESC, record.createdAt DESC, record.id DESC
            """)
    Page<ActualRecord> search(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("categoryId") UUID categoryId,
            @Param("recordType") ActualRecordType recordType,
            @Param("status") ActualRecordStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    @Query("""
            SELECT SUM(record.actualMinutes)
            FROM ActualRecord record
            WHERE record.ownerId = :ownerId
              AND record.status = com.lifebalance.analytics.domain.ActualRecordStatus.ACTIVE
              AND (:taskId IS NULL OR record.taskId = :taskId)
              AND (:capitalCycleId IS NULL OR record.capitalCycleId = :capitalCycleId)
              AND (:from IS NULL OR record.actualDate >= :from)
              AND (:to IS NULL OR record.actualDate <= :to)
            """)
    Long sumActualMinutes(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT SUM(record.actualCost)
            FROM ActualRecord record
            WHERE record.ownerId = :ownerId
              AND record.status = com.lifebalance.analytics.domain.ActualRecordStatus.ACTIVE
              AND (:taskId IS NULL OR record.taskId = :taskId)
              AND (:capitalCycleId IS NULL OR record.capitalCycleId = :capitalCycleId)
              AND (:currencyCode IS NULL OR record.currencyCode = :currencyCode)
              AND (:from IS NULL OR record.actualDate >= :from)
              AND (:to IS NULL OR record.actualDate <= :to)
            """)
    BigDecimal sumActualCost(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("currencyCode") String currencyCode,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

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
}
