package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MoneyCapitalRepository extends JpaRepository<MoneyCapital, UUID> {

    Optional<MoneyCapital> findByCapitalCycleId(UUID capitalCycleId);

    boolean existsByCapitalCycleId(UUID capitalCycleId);
}
