package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.BudgetStatus;
import com.lifebalance.finance.domain.FinanceBudget;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinanceBudgetRepository extends JpaRepository<FinanceBudget, UUID> {

    Optional<FinanceBudget> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            select budget
            from FinanceBudget budget
            where budget.ownerId = :ownerId
              and (:status is null or budget.status = :status)
              and (:currencyCode is null or budget.currencyCode = :currencyCode)
              and (:categoryId is null or budget.category.id = :categoryId)
              and (:fromDate is null or budget.periodEnd >= :fromDate)
              and (:toDate is null or budget.periodStart <= :toDate)
            """)
    Page<FinanceBudget> search(
            @Param("ownerId") UUID ownerId,
            @Param("status") BudgetStatus status,
            @Param("currencyCode") String currencyCode,
            @Param("categoryId") UUID categoryId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    default boolean existsOverlappingActiveBudget(
            UUID ownerId,
            UUID categoryId,
            String currencyCode,
            LocalDate periodStart,
            LocalDate periodEnd,
            UUID excludeBudgetId
    ) {
        return existsOverlappingBudget(
                ownerId,
                categoryId,
                currencyCode,
                BudgetStatus.ACTIVE,
                periodStart,
                periodEnd,
                excludeBudgetId
        );
    }

    @Query("""
            select case when count(budget) > 0 then true else false end
            from FinanceBudget budget
            where budget.ownerId = :ownerId
              and budget.status = :status
              and budget.currencyCode = :currencyCode
              and ((:categoryId is null and budget.category is null)
                   or (:categoryId is not null and budget.category.id = :categoryId))
              and budget.periodStart <= :periodEnd
              and budget.periodEnd >= :periodStart
              and (:excludeBudgetId is null or budget.id <> :excludeBudgetId)
            """)
    boolean existsOverlappingBudget(
            @Param("ownerId") UUID ownerId,
            @Param("categoryId") UUID categoryId,
            @Param("currencyCode") String currencyCode,
            @Param("status") BudgetStatus status,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("excludeBudgetId") UUID excludeBudgetId);
}
