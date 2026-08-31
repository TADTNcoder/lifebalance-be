package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceIncomeSourceType;
import com.lifebalance.finance.domain.FinanceMonthlyJarSettlement;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.FinancialTransaction;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceMonthlyJarSettlementRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceMonthEndJarSettlementWorker {

    static final String SETTLEMENT_TRANSACTION_NAME = "Hoàn quỹ cuối tháng";
    static final String DEFICIT_TRANSACTION_NAME = "Bù chênh lệch cuối tháng";

    private static final Logger log = LoggerFactory.getLogger(FinanceMonthEndJarSettlementWorker.class);

    private final FinanceAccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final FinanceMonthlyJarSettlementRepository settlementRepository;
    private final FinanceHistoryRecorder historyRecorder;

    public FinanceMonthEndJarSettlementWorker(
            FinanceAccountRepository accountRepository,
            FinancialTransactionRepository transactionRepository,
            FinanceMonthlyJarSettlementRepository settlementRepository,
            FinanceHistoryRecorder historyRecorder
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.settlementRepository = settlementRepository;
        this.historyRecorder = historyRecorder;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean settleJar(
            UUID jarId,
            UUID ownerId,
            OffsetDateTime currentMonthStart,
            OffsetDateTime settledAt
    ) {
        FinanceAccount jar = accountRepository
                .findByIdAndOwnerIdForUpdate(jarId, ownerId)
                .orElse(null);
        if (jar == null || jar.getCreatedAt() == null) {
            return false;
        }

        FinanceAccountMonthPolicy.MonthRange jarMonth =
                FinanceAccountMonthPolicy.monthContaining(jar.getCreatedAt());
        LocalDate periodStart = jarMonth.startInclusive().toLocalDate();
        if (!jarMonth.endExclusive().isBefore(currentMonthStart)
                && !jarMonth.endExclusive().isEqual(currentMonthStart)) {
            return false;
        }
        if (settlementRepository.existsByJarAccountIdAndPeriodStart(jar.getId(), periodStart)) {
            return false;
        }

        FinanceAccount mainPool = accountRepository
                .findFirstByOwnerIdAndAccountTypeAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        jar.getOwnerId(),
                        com.lifebalance.finance.domain.FinanceAccountType.MAIN_POOL,
                        com.lifebalance.finance.domain.FinanceAccountStatus.ACTIVE,
                        jarMonth.startInclusive(),
                        jarMonth.endExclusive()
                )
                .orElse(null);
        if (mainPool == null) {
            log.warn(
                    "Cannot settle finance jar {} for {} because its monthly main pool is missing",
                    jar.getId(),
                    periodStart
            );
            return false;
        }

        BigDecimal allocatedAmount = jar.getOpeningBalance();
        BigDecimal actualExpenseAmount = transactionRepository.sumPostedExpenseBySourceAccount(
                jar.getOwnerId(),
                jar.getId(),
                jar.getCurrencyCode(),
                jarMonth.startInclusive(),
                jarMonth.endExclusive()
        );
        BigDecimal closingBalance = jar.getCurrentBalance();
        BigDecimal transferredAmount = closingBalance.abs();
        BigDecimal varianceAmount = closingBalance.negate();
        FinancialTransaction settlementTransaction = createSettlementTransfer(
                jar,
                mainPool,
                closingBalance,
                jarMonth.endExclusive().minusNanos(1),
                periodStart
        );

        FinanceMonthlyJarSettlement settlement = FinanceMonthlyJarSettlement.create(
                jar.getOwnerId(),
                periodStart,
                jar.getCurrencyCode(),
                mainPool,
                jar,
                settlementTransaction,
                allocatedAmount,
                actualExpenseAmount,
                closingBalance,
                transferredAmount,
                varianceAmount,
                settledAt
        );
        settlementRepository.save(settlement);
        return true;
    }

    private FinancialTransaction createSettlementTransfer(
            FinanceAccount jar,
            FinanceAccount mainPool,
            BigDecimal closingBalance,
            OffsetDateTime transactionDate,
            LocalDate periodStart
    ) {
        if (closingBalance.signum() == 0) {
            return null;
        }

        FinanceAccount sourceAccount;
        FinanceAccount destinationAccount;
        String transactionName;
        if (closingBalance.signum() > 0) {
            sourceAccount = jar;
            destinationAccount = mainPool;
            jar.debit(closingBalance);
            mainPool.credit(closingBalance);
            transactionName = SETTLEMENT_TRANSACTION_NAME;
        } else {
            sourceAccount = mainPool;
            destinationAccount = jar;
            BigDecimal deficit = closingBalance.abs();
            mainPool.debit(deficit);
            jar.credit(deficit);
            transactionName = DEFICIT_TRANSACTION_NAME;
        }

        FinancialTransaction transaction = FinancialTransaction.post(
                jar.getOwnerId(),
                jar.getOwnerId(),
                FinanceTransactionType.TRANSFER,
                sourceAccount,
                destinationAccount,
                null,
                closingBalance.abs(),
                jar.getCurrencyCode(),
                transactionDate,
                transactionName,
                "Tự động quyết toán hũ cho tháng " + periodStart.toString().substring(0, 7),
                FinanceIncomeSourceType.ONE_OFF,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        transaction.markSystemGenerated();
        transaction = transactionRepository.save(transaction);

        historyRecorder.record(
                jar.getOwnerId(),
                jar.getOwnerId(),
                FinanceHistoryActionType.TRANSACTION_CREATED,
                FinanceReferenceType.FINANCIAL_TRANSACTION,
                transaction.getId(),
                "Tự động quyết toán cuối tháng",
                null,
                "type=TRANSFER;systemGenerated=true;amount=" + transaction.getAmount()
        );
        return transaction;
    }
}
