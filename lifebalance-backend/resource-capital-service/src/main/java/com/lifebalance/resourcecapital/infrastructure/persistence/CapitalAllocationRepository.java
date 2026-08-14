package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapitalAllocationRepository extends JpaRepository<CapitalAllocation, UUID> {

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
            order by allocation.createdAt desc
            """)
    List<CapitalAllocation> findByCapitalCycleId(@Param("capitalCycleId") UUID capitalCycleId);

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
              and allocation.status = :status
            order by allocation.createdAt desc
            """)
    List<CapitalAllocation> findByCapitalCycleIdAndStatus(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("status") AllocationStatus status
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.status = :status
            order by allocation.createdAt desc
            """)
    List<CapitalAllocation> findByCapitalCycleIdAndCapitalTypeAndStatus(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("status") AllocationStatus status
    );

    List<CapitalAllocation> findByCapitalTypeAndStatus(CapitalKind capitalType, AllocationStatus status);

    List<CapitalAllocation> findByTargetTypeAndTargetIdAndStatus(
            AllocationTargetType targetType,
            UUID targetId,
            AllocationStatus status
    );

    List<CapitalAllocation> findByTargetTypeAndTargetIdAndStatusAndCapitalType(
            AllocationTargetType targetType,
            UUID targetId,
            AllocationStatus status,
            CapitalKind capitalType
    );

    default List<CapitalAllocation> findByTaskIdAndStatus(UUID taskId, AllocationStatus status) {
        return findByTargetTypeAndTargetIdAndStatus(AllocationTargetType.TASK, taskId, status);
    }

    default List<CapitalAllocation> findByTaskIdAndStatusAndCapitalType(
            UUID taskId,
            AllocationStatus status,
            CapitalKind capitalType
    ) {
        return findByTargetTypeAndTargetIdAndStatusAndCapitalType(
                AllocationTargetType.TASK,
                taskId,
                status,
                capitalType
        );
    }

    Optional<CapitalAllocation> findByCapitalCycleIdAndCapitalTypeAndTargetTypeAndTargetId(
            UUID capitalCycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.targetType = :targetType
              and allocation.targetId = :targetId
            """)
    Optional<CapitalAllocation> findTargetForUpdate(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetId") UUID targetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.targetType = :targetType
              and allocation.targetId in :targetIds
            order by allocation.targetType asc, allocation.targetId asc
            """)
    List<CapitalAllocation> findTargetsForUpdate(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetIds") Collection<UUID> targetIds
    );

    @Query("""
            select coalesce(sum(allocation.allocatedAmount), 0)
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
            """)
    BigDecimal sumAllocatedAmount(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType
    );

    @Query("""
            select allocation.capitalType as capitalType,
                   allocation.targetType as targetType,
                   allocation.targetId as targetId,
                   coalesce(sum(allocation.allocatedAmount), 0) as allocatedAmount
            from CapitalAllocation allocation
            where allocation.capitalCycle.id = :capitalCycleId
              and allocation.targetType = :targetType
            group by allocation.capitalType, allocation.targetType, allocation.targetId
            order by allocation.capitalType asc, sum(allocation.allocatedAmount) desc, allocation.targetId asc
            """)
    List<TargetAllocationBreakdownProjection> findAllocationBreakdownByTargetType(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("targetType") AllocationTargetType targetType
    );

    interface TargetAllocationBreakdownProjection {

        CapitalKind getCapitalType();

        AllocationTargetType getTargetType();

        UUID getTargetId();

        BigDecimal getAllocatedAmount();
    }
}
