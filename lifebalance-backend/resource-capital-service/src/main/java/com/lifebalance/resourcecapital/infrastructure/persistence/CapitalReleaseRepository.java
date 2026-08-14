package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitalrelease.CapitalRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CapitalReleaseRepository extends JpaRepository<CapitalRelease, Long> {

    @Query("""
            select capitalRelease
            from CapitalRelease capitalRelease
            where capitalRelease.allocation.id = :allocationId
            order by capitalRelease.releasedAt desc, capitalRelease.id desc
            """)
    List<CapitalRelease> findByAllocationId(@Param("allocationId") UUID allocationId);
}
