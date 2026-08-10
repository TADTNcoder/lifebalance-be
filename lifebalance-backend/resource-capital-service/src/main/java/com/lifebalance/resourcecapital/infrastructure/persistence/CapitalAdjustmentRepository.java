package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CapitalAdjustmentRepository extends JpaRepository<CapitalAdjustment, Long> {

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.capitalCycle.id = :capitalCycleId
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    List<CapitalAdjustment> findByCapitalCycleId(@Param("capitalCycleId") UUID capitalCycleId);

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.capitalCycle.id = :capitalCycleId
              and adjustment.capitalType = :capitalType
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    List<CapitalAdjustment> findByCapitalCycleIdAndCapitalType(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalKind capitalType
    );
}
