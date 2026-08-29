package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapitalCycleRepository extends JpaRepository<CapitalCycle, UUID> {

    Optional<CapitalCycle> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<CapitalCycle> findByOwnerId(UUID ownerId, Pageable pageable);

    @Query("""
            select cycle
            from CapitalCycle cycle
            where cycle.ownerId = :ownerId
              and (:type is null or cycle.type = :type)
              and (:status is null or cycle.status = :status)
              and (:fromDate is null or cycle.endDate >= :fromDate)
              and (:toDate is null or cycle.startDate <= :toDate)
            """)
    Page<CapitalCycle> searchOwnedCycles(
            @Param("ownerId") UUID ownerId,
            @Param("type") CapitalCycleType type,
            @Param("status") CapitalCycleStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cycle
            from CapitalCycle cycle
            where cycle.id = :id
              and cycle.ownerId = :ownerId
            """)
    Optional<CapitalCycle> findByIdAndOwnerIdForUpdate(
            @Param("id") UUID id,
            @Param("ownerId") UUID ownerId
    );

    Page<CapitalCycle> findByOwnerIdAndStatus(UUID ownerId, CapitalCycleStatus status, Pageable pageable);

    Optional<CapitalCycle> findFirstByOwnerIdAndStatusOrderByActivatedAtDescCreatedAtDesc(
            UUID ownerId,
            CapitalCycleStatus status
    );

    Optional<CapitalCycle> findByOwnerIdAndTypeAndStatus(
            UUID ownerId,
            CapitalCycleType type,
            CapitalCycleStatus status
    );

    boolean existsByOwnerIdAndTypeAndStatus(
            UUID ownerId,
            CapitalCycleType type,
            CapitalCycleStatus status
    );

    boolean existsByOwnerIdAndTypeAndStatusAndIdNot(
            UUID ownerId,
            CapitalCycleType type,
            CapitalCycleStatus status,
            UUID excludedId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cycle
            from CapitalCycle cycle
            where cycle.ownerId = :ownerId
              and cycle.type = :type
            """)
    List<CapitalCycle> findByOwnerIdAndTypeForUpdate(
            @Param("ownerId") UUID ownerId,
            @Param("type") CapitalCycleType type
    );

    /**
     * Checks overlap without sending a nullable UUID parameter to PostgreSQL.
     *
     * Hibernate/PostgreSQL can fail to infer the SQL type of a parameter used in
     * an expression such as "? is null". Create always has no excluded cycle,
     * so dispatch to a query that does not bind an excluded id at all.
     */
    default boolean existsOverlappingCycle(
            UUID ownerId,
            CapitalCycleType type,
            LocalDate startDate,
            LocalDate endDate,
            UUID excludedCycleId
    ) {
        if (excludedCycleId == null) {
            return existsOverlappingCycleForCreate(ownerId, type, startDate, endDate);
        }
        return existsOverlappingCycleForUpdate(ownerId, type, startDate, endDate, excludedCycleId);
    }

    @Query("""
            select case when count(cycle) > 0 then true else false end
            from CapitalCycle cycle
            where cycle.ownerId = :ownerId
              and cycle.type = :type
              and cycle.startDate <= :endDate
              and cycle.endDate >= :startDate
            """)
    boolean existsOverlappingCycleForCreate(
            @Param("ownerId") UUID ownerId,
            @Param("type") CapitalCycleType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select case when count(cycle) > 0 then true else false end
            from CapitalCycle cycle
            where cycle.ownerId = :ownerId
              and cycle.type = :type
              and cycle.startDate <= :endDate
              and cycle.endDate >= :startDate
              and cycle.id <> :excludedCycleId
            """)
    boolean existsOverlappingCycleForUpdate(
            @Param("ownerId") UUID ownerId,
            @Param("type") CapitalCycleType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCycleId") UUID excludedCycleId
    );
}
