package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TimeCapitalRepository extends JpaRepository<TimeCapital, UUID> {

    Optional<TimeCapital> findByCapitalCycleId(UUID capitalCycleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select timeCapital
            from TimeCapital timeCapital
            where timeCapital.capitalCycle.id = :capitalCycleId
            """)
    Optional<TimeCapital> findByCapitalCycleIdForUpdate(@Param("capitalCycleId") UUID capitalCycleId);

    boolean existsByCapitalCycleId(UUID capitalCycleId);
}
