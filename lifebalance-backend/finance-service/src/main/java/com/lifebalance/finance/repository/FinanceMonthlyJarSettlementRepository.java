package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.FinanceMonthlyJarSettlement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceMonthlyJarSettlementRepository
        extends JpaRepository<FinanceMonthlyJarSettlement, UUID> {

    boolean existsByJarAccountIdAndPeriodStart(UUID jarAccountId, LocalDate periodStart);

    List<FinanceMonthlyJarSettlement>
    findByOwnerIdAndCurrencyCodeAndPeriodStartBetweenOrderByPeriodStartAsc(
            UUID ownerId,
            String currencyCode,
            LocalDate fromPeriod,
            LocalDate toPeriod
    );
}
