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

    @Query("""
            select case when count(cycle) > 0 then true else false end
            from CapitalCycle cycle
            where cycle.ownerId = :ownerId
              and cycle.type = :type
              and cycle.startDate <= :endDate
              and cycle.endDate >= :startDate
              and (
                  :excludedCycleId is null
                  or cycle.id <> :excludedCycleId
              )
            """)
    boolean existsOverlappingCycle(
            @Param("ownerId") UUID ownerId,
            @Param("type") CapitalCycleType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedCycleId") UUID excludedCycleId
    );
}
