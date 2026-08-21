package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.RecurringTransactionRule;
import com.lifebalance.finance.domain.RecurringTransactionStatus;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringTransactionRuleRepository extends JpaRepository<RecurringTransactionRule, UUID> {

    Optional<RecurringTransactionRule> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            select rule
            from RecurringTransactionRule rule
            where rule.ownerId = :ownerId
              and (:status is null or rule.status = :status)
              and (:dueOnOrBefore is null or rule.nextRunDate <= :dueOnOrBefore)
            """)
    Page<RecurringTransactionRule> search(
            @Param("ownerId") UUID ownerId,
            @Param("status") RecurringTransactionStatus status,
            @Param("dueOnOrBefore") LocalDate dueOnOrBefore,
            Pageable pageable);
}
