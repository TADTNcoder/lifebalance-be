package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitaladjustment.AdjustmentType;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CapitalAdjustmentRepository extends
        JpaRepository<CapitalAdjustment, Long>,
        JpaSpecificationExecutor<CapitalAdjustment> {

    /**
     * Reads adjustment history only inside the owner boundary to avoid cross-user data exposure.
     */
    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.userId = :userId
              and adjustment.capitalCycle.id = :capitalCycleId
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    Page<CapitalAdjustment> findByUserIdAndCapitalCycleId(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            Pageable pageable
    );

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.userId = :userId
              and adjustment.capitalCycle.id = :capitalCycleId
              and adjustment.capitalType = :capitalType
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    Page<CapitalAdjustment> findByUserIdAndCapitalCycleIdAndCapitalType(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalType capitalType,
            Pageable pageable
    );

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.userId = :userId
              and adjustment.capitalCycle.id = :capitalCycleId
              and adjustment.adjustmentType = :adjustmentType
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    Page<CapitalAdjustment> findByUserIdAndCapitalCycleIdAndAdjustmentType(
            @Param("userId") UUID userId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("adjustmentType") AdjustmentType adjustmentType,
            Pageable pageable
    );

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.userId = :userId
              and adjustment.createdAt between :startDate and :endDate
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    Page<CapitalAdjustment> findByUserIdAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    boolean existsByUserIdAndCapitalCycleId(UUID userId, UUID capitalCycleId);
}
