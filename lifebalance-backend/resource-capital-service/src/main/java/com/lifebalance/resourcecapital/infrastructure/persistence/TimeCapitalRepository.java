package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TimeCapitalRepository extends JpaRepository<TimeCapital, UUID> {

    Optional<TimeCapital> findByCapitalCycleId(UUID capitalCycleId);

    boolean existsByCapitalCycleId(UUID capitalCycleId);
}
