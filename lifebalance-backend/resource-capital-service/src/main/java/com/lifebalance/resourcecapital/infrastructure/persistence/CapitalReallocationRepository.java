package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitalreallocation.CapitalReallocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CapitalReallocationRepository extends JpaRepository<CapitalReallocation, Long> {

    @Query("""
            select reallocation
            from CapitalReallocation reallocation
            where reallocation.fromAllocation.id = :fromAllocationId
            order by reallocation.createdAt desc, reallocation.id desc
            """)
    List<CapitalReallocation> findByFromAllocationId(@Param("fromAllocationId") UUID fromAllocationId);

    @Query("""
            select reallocation
            from CapitalReallocation reallocation
            where reallocation.toAllocation.id = :toAllocationId
            order by reallocation.createdAt desc, reallocation.id desc
            """)
    List<CapitalReallocation> findByToAllocationId(@Param("toAllocationId") UUID toAllocationId);

    @Query("""
            select reallocation
            from CapitalReallocation reallocation
            where reallocation.fromAllocation.id = :allocationId
               or reallocation.toAllocation.id = :allocationId
            order by reallocation.createdAt desc, reallocation.id desc
            """)
    List<CapitalReallocation> findByAllocationId(@Param("allocationId") UUID allocationId);
}
