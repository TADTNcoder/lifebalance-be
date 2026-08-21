package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinanceAccountRepository extends JpaRepository<FinanceAccount, UUID> {

    Optional<FinanceAccount> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select account
            from FinanceAccount account
            where account.id = :id
              and account.ownerId = :ownerId
            """)
    Optional<FinanceAccount> findByIdAndOwnerIdForUpdate(
            @Param("id") UUID id,
            @Param("ownerId") UUID ownerId);

    boolean existsByOwnerIdAndNameIgnoreCaseAndStatus(
            UUID ownerId,
            String name,
            FinanceAccountStatus status);

    @Query("""
            select account
            from FinanceAccount account
            where account.ownerId = :ownerId
              and (:status is null or account.status = :status)
              and (:currencyCode is null or account.currencyCode = :currencyCode)
            """)
    Page<FinanceAccount> search(
            @Param("ownerId") UUID ownerId,
            @Param("status") FinanceAccountStatus status,
            @Param("currencyCode") String currencyCode,
            Pageable pageable);

    default BigDecimal sumActiveBalance(UUID ownerId, String currencyCode) {
        return sumBalanceByStatus(ownerId, currencyCode, FinanceAccountStatus.ACTIVE);
    }

    @Query("""
            select coalesce(sum(account.currentBalance), 0)
            from FinanceAccount account
            where account.ownerId = :ownerId
              and account.status = :status
              and account.currencyCode = :currencyCode
            """)
    BigDecimal sumBalanceByStatus(
            @Param("ownerId") UUID ownerId,
            @Param("currencyCode") String currencyCode,
            @Param("status") FinanceAccountStatus status);
}
