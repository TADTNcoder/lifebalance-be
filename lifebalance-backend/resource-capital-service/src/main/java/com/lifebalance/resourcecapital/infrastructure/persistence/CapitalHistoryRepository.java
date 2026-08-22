package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface CapitalHistoryRepository extends
        JpaRepository<CapitalHistory, UUID>,
        JpaSpecificationExecutor<CapitalHistory> {

    @Query("""
            select history
            from CapitalHistory history
            where history.capitalCycle.id = :capitalCycleId
            order by history.createdAt desc, history.id desc
            """)
    Page<CapitalHistory> findByCapitalCycleId(
            @Param("capitalCycleId") UUID capitalCycleId,
            Pageable pageable
    );

    boolean existsByCapitalCycleId(UUID capitalCycleId);

    @Query("""
            select history
            from CapitalHistory history
            where history.capitalCycle.id = :capitalCycleId
              and history.capitalType = :capitalType
            order by history.createdAt desc, history.id desc
            """)
    Page<CapitalHistory> findByCapitalCycleIdAndCapitalType(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            Pageable pageable
    );

    @Query("""
            select history
            from CapitalHistory history
            where history.capitalCycle.id = :capitalCycleId
              and history.actionType = :actionType
            order by history.createdAt desc, history.id desc
            """)
    Page<CapitalHistory> findByCapitalCycleIdAndActionType(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("actionType") CapitalActionType actionType,
            Pageable pageable
    );

    @Query("""
            select history
            from CapitalHistory history
            where history.capitalCycle.id = :capitalCycleId
              and history.referenceType = :referenceType
              and history.referenceId = :referenceId
            order by history.createdAt desc, history.id desc
            """)
    Page<CapitalHistory> findByCapitalCycleIdAndReferenceTypeAndReferenceId(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("referenceType") CapitalReferenceType referenceType,
            @Param("referenceId") UUID referenceId,
            Pageable pageable
    );

    @Query("""
            select history
            from CapitalHistory history
            where history.capitalCycle.id = :capitalCycleId
              and history.createdAt >= :from
              and history.createdAt < :to
            order by history.createdAt desc, history.id desc
            """)
    Page<CapitalHistory> findByCapitalCycleIdAndCreatedAtRange(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}
