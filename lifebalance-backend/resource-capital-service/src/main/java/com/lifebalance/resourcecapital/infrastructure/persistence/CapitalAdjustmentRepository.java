package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CapitalAdjustmentRepository extends
        JpaRepository<CapitalAdjustment, Long>,
        JpaSpecificationExecutor<CapitalAdjustment> {

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.capitalCycle.id = :capitalCycleId
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    List<CapitalAdjustment> findByCapitalCycleId(@Param("capitalCycleId") UUID capitalCycleId);

    default List<CapitalAdjustment> findByCapitalCycleIdAndCapitalType(UUID capitalCycleId, CapitalKind capitalType) {
        return findByCapitalCycleIdAndCapitalTypeValue(capitalCycleId, CapitalType.from(capitalType));
    }

    @Query("""
            select adjustment
            from CapitalAdjustment adjustment
            where adjustment.capitalCycle.id = :capitalCycleId
              and adjustment.capitalType = :capitalType
            order by adjustment.createdAt desc, adjustment.id desc
            """)
    List<CapitalAdjustment> findByCapitalCycleIdAndCapitalTypeValue(
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("capitalType") CapitalType capitalType
    );
}
