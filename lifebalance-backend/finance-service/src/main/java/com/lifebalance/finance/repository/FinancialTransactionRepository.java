package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.FinanceTransactionStatus;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.FinancialTransaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    Optional<FinancialTransaction> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            select transaction
            from FinancialTransaction transaction
            left join fetch transaction.sourceAccount
            left join fetch transaction.destinationAccount
            left join fetch transaction.category
            where transaction.id = :id
              and transaction.ownerId = :ownerId
            """)
    Optional<FinancialTransaction> findDetailedByIdAndOwnerId(
            @Param("id") UUID id,
            @Param("ownerId") UUID ownerId);

    @Query("""
            select transaction
            from FinancialTransaction transaction
            where transaction.ownerId = :ownerId
              and (:type is null or transaction.transactionType = :type)
              and (:status is null or transaction.status = :status)
              and (:accountId is null
                   or transaction.sourceAccount.id = :accountId
                   or transaction.destinationAccount.id = :accountId)
              and (:categoryId is null or transaction.category.id = :categoryId)
              and (:taskId is null or transaction.taskId = :taskId)
              and (:capitalCycleId is null or transaction.capitalCycleId = :capitalCycleId)
              and (:fromDate is null or transaction.transactionDate >= :fromDate)
              and (:toDate is null or transaction.transactionDate <= :toDate)
            """)
    Page<FinancialTransaction> search(
            @Param("ownerId") UUID ownerId,
            @Param("type") FinanceTransactionType type,
            @Param("status") FinanceTransactionStatus status,
            @Param("accountId") UUID accountId,
            @Param("categoryId") UUID categoryId,
            @Param("taskId") UUID taskId,
            @Param("capitalCycleId") UUID capitalCycleId,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            Pageable pageable);

    default BigDecimal sumPostedAmount(
            UUID ownerId,
            FinanceTransactionType type,
            String currencyCode,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            UUID categoryId
    ) {
        return sumAmount(ownerId, type, FinanceTransactionStatus.POSTED, currencyCode, fromDate, toDate, categoryId);
    }

    @Query("""
            select coalesce(sum(transaction.amount), 0)
            from FinancialTransaction transaction
            where transaction.ownerId = :ownerId
              and transaction.status = :status
              and transaction.transactionType = :type
              and transaction.currencyCode = :currencyCode
              and transaction.transactionDate >= :fromDate
              and transaction.transactionDate <= :toDate
              and (:categoryId is null or transaction.category.id = :categoryId)
            """)
    BigDecimal sumAmount(
            @Param("ownerId") UUID ownerId,
            @Param("type") FinanceTransactionType type,
            @Param("status") FinanceTransactionStatus status,
            @Param("currencyCode") String currencyCode,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            @Param("categoryId") UUID categoryId);
}
