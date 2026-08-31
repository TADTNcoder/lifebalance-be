package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.domain.FinanceAccountType;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinanceAccountRepository extends JpaRepository<FinanceAccount, UUID> {

    Optional<FinanceAccount> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            select account
            from FinanceAccount account
            where account.accountType = com.lifebalance.finance.domain.FinanceAccountType.JAR
              and account.status = com.lifebalance.finance.domain.FinanceAccountStatus.ACTIVE
              and account.createdAt < :createdBefore
            order by account.createdAt, account.id
            """)
    List<FinanceAccount> findExpiredActiveJars(@Param("createdBefore") OffsetDateTime createdBefore);

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

    @Query("""
            select (count(account) > 0)
            from FinanceAccount account
            where account.ownerId = :ownerId
              and lower(account.name) = lower(:name)
              and account.status = :status
              and account.createdAt >= :createdFrom
              and account.createdAt < :createdTo
            """)
    boolean existsNameInCreatedPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("name") String name,
            @Param("status") FinanceAccountStatus status,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo);

    @Query("""
            select (count(account) > 0)
            from FinanceAccount account
            where account.ownerId = :ownerId
              and account.accountType = :accountType
              and account.status = :status
              and account.createdAt >= :createdFrom
              and account.createdAt < :createdTo
            """)
    boolean existsTypeInCreatedPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("accountType") FinanceAccountType accountType,
            @Param("status") FinanceAccountStatus status,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FinanceAccount>
    findFirstByOwnerIdAndAccountTypeAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            UUID ownerId,
            FinanceAccountType accountType,
            FinanceAccountStatus status,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo);

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

    default BigDecimal sumActiveBalanceByType(
            UUID ownerId,
            String currencyCode,
            FinanceAccountType accountType,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo
    ) {
        return sumBalanceByStatusAndAccountTypeInCreatedPeriod(
                ownerId,
                currencyCode,
                FinanceAccountStatus.ACTIVE,
                accountType,
                createdFrom,
                createdTo
        );
    }

    default BigDecimal sumActiveOpeningBalance(
            UUID ownerId,
            String currencyCode,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo
    ) {
        return sumOpeningBalanceByStatusInCreatedPeriod(
                ownerId,
                currencyCode,
                FinanceAccountStatus.ACTIVE,
                createdFrom,
                createdTo
        );
    }

    @Query("""
            select coalesce(sum(account.currentBalance), 0)
            from FinanceAccount account
            where account.ownerId = :ownerId
              and account.status = :status
              and account.currencyCode = :currencyCode
              and account.accountType = :accountType
              and account.createdAt >= :createdFrom
              and account.createdAt < :createdTo
            """)
    BigDecimal sumBalanceByStatusAndAccountTypeInCreatedPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("currencyCode") String currencyCode,
            @Param("status") FinanceAccountStatus status,
            @Param("accountType") FinanceAccountType accountType,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo);

    @Query("""
            select coalesce(sum(account.openingBalance), 0)
            from FinanceAccount account
            where account.ownerId = :ownerId
              and account.status = :status
              and account.currencyCode = :currencyCode
              and account.createdAt >= :createdFrom
              and account.createdAt < :createdTo
            """)
    BigDecimal sumOpeningBalanceByStatusInCreatedPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("currencyCode") String currencyCode,
            @Param("status") FinanceAccountStatus status,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo);
}
