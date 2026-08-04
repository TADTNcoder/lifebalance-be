package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MoneyCapitalRepository extends JpaRepository<MoneyCapital, UUID> {

    Optional<MoneyCapital> findByCapitalCycleId(UUID capitalCycleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select moneyCapital
            from MoneyCapital moneyCapital
            where moneyCapital.capitalCycle.id = :capitalCycleId
            """)
    Optional<MoneyCapital> findByCapitalCycleIdForUpdate(@Param("capitalCycleId") UUID capitalCycleId);

    boolean existsByCapitalCycleId(UUID capitalCycleId);
}
