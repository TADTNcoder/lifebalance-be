package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapitalAllocationRepository extends
        JpaRepository<CapitalAllocation, UUID>,
        JpaSpecificationExecutor<CapitalAllocation> {

    Optional<CapitalAllocation> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.id = :id
              and allocation.userId = :userId
            """)
    Optional<CapitalAllocation> findByIdAndUserIdForUpdate(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );

    /**
     * Reads allocation records only inside the owner boundary to avoid cross-user data exposure.
     */
    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
            order by allocation.createdAt desc, allocation.id desc
            """)
    List<CapitalAllocation> findByUserIdAndCapitalCycleId(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.status = :status
            order by allocation.createdAt desc, allocation.id desc
            """)
    List<CapitalAllocation> findByUserIdAndCapitalCycleIdAndStatus(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("status") AllocationStatus status
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
            order by allocation.createdAt desc, allocation.id desc
            """)
    Page<CapitalAllocation> findByUserIdAndCapitalCycleIdAndCapitalType(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            Pageable pageable
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.targetType = :targetType
              and allocation.targetId = :targetId
              and allocation.capitalType = :capitalType
            """)
    Optional<CapitalAllocation> findByUserIdAndCapitalCycleIdAndTargetTypeAndTargetIdAndCapitalType(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetId") UUID targetId,
            @Param("capitalType") CapitalKind capitalType
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.status = :status
              and (allocation.allocatedAmount - allocation.spentAmount) > 0
            order by allocation.createdAt desc, allocation.id desc
            """)
    List<CapitalAllocation> findAvailableForReallocateOrRelease(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("status") AllocationStatus status
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.targetType = :targetType
              and allocation.targetId = :targetId
              and allocation.status = :status
            order by allocation.createdAt desc, allocation.id desc
            """)
    List<CapitalAllocation> findByUserIdAndTargetTypeAndTargetIdAndStatus(
            @Param("userId") UUID userId,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetId") UUID targetId,
            @Param("status") AllocationStatus status
    );

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.targetType = :targetType
              and allocation.targetId = :targetId
              and allocation.status = :status
              and allocation.capitalType = :capitalType
            order by allocation.createdAt desc, allocation.id desc
            """)
    List<CapitalAllocation> findByUserIdAndTargetTypeAndTargetIdAndStatusAndCapitalType(
            @Param("userId") UUID userId,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetId") UUID targetId,
            @Param("status") AllocationStatus status,
            @Param("capitalType") CapitalKind capitalType
    );

    default List<CapitalAllocation> findByUserIdAndTaskIdAndStatus(
            UUID userId,
            UUID taskId,
            AllocationStatus status
    ) {
        return findByUserIdAndTargetTypeAndTargetIdAndStatus(userId, AllocationTargetType.TASK, taskId, status);
    }

    default List<CapitalAllocation> findByUserIdAndTaskIdAndStatusAndCapitalType(
            UUID userId,
            UUID taskId,
            AllocationStatus status,
            CapitalKind capitalType
    ) {
        return findByUserIdAndTargetTypeAndTargetIdAndStatusAndCapitalType(
                userId,
                AllocationTargetType.TASK,
                taskId,
                status,
                capitalType
        );
    }

    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.status = :status
            order by allocation.createdAt desc, allocation.id desc
            """)
    List<CapitalAllocation> findByUserIdAndCapitalCycleIdAndCapitalTypeAndStatus(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("status") AllocationStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.targetType = :targetType
              and allocation.targetId = :targetId
            """)
    Optional<CapitalAllocation> findTargetForUpdate(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetId") UUID targetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select allocation
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.targetType = :targetType
              and allocation.targetId in :targetIds
            order by allocation.targetType asc, allocation.targetId asc
            """)
    List<CapitalAllocation> findTargetsForUpdate(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("targetType") AllocationTargetType targetType,
            @Param("targetIds") Collection<UUID> targetIds
    );

    @Query("""
            select coalesce(sum(allocation.allocatedAmount), 0)
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
            """)
    BigDecimal sumAllocatedAmount(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType
    );

    @Query("""
            select coalesce(sum(allocation.allocatedAmount), 0)
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.status = :status
            """)
    BigDecimal sumAllocatedAmount(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("status") AllocationStatus status
    );

    @Query("""
            select coalesce(sum(allocation.spentAmount), 0)
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.capitalType = :capitalType
              and allocation.status = :status
            """)
    BigDecimal sumSpentAmount(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType,
            @Param("status") AllocationStatus status
    );

    @Query("""
            select allocation.capitalType as capitalType,
                   allocation.targetType as targetType,
                   allocation.targetId as targetId,
                   coalesce(sum(allocation.allocatedAmount), 0) as allocatedAmount
            from CapitalAllocation allocation
            where allocation.userId = :userId
              and allocation.capitalCycle.id = :capitalCycleId
              and allocation.targetType = :targetType
            group by allocation.capitalType, allocation.targetType, allocation.targetId
            order by allocation.capitalType asc, sum(allocation.allocatedAmount) desc, allocation.targetId asc
            """)
    List<TargetAllocationBreakdownProjection> findAllocationBreakdownByUserIdAndTargetType(
            @Param("userId") UUID userId,
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
