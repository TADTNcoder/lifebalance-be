package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.domain.FinanceMonthlyJarSettlement;
import com.lifebalance.finance.dto.FinanceMonthlyJarSettlementSummaryResponse;
import com.lifebalance.finance.dto.FinanceSummaryResponse;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceMonthlyJarSettlementRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import com.lifebalance.finance.service.FinanceReportService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceReportServiceImpl implements FinanceReportService {

    private static final OffsetDateTime EARLIEST_TRANSACTION_DATE =
            OffsetDateTime.parse("0001-01-01T00:00:00Z");

    private final FinancialTransactionRepository transactionRepository;
    private final FinanceAccountRepository accountRepository;
    private final FinanceMonthlyJarSettlementRepository settlementRepository;

    public FinanceReportServiceImpl(
            FinancialTransactionRepository transactionRepository,
            FinanceAccountRepository accountRepository,
            FinanceMonthlyJarSettlementRepository settlementRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.settlementRepository = settlementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceSummaryResponse getSummary(
            UUID ownerId,
            String currencyCode,
            OffsetDateTime fromDate,
            OffsetDateTime toDate
    ) {
        String normalizedCurrency = FinanceSupport.normalizeCurrency(currencyCode);
        OffsetDateTime normalizedTo = toDate == null ? OffsetDateTime.now() : toDate;
        FinanceAccountMonthPolicy.MonthRange defaultMonth =
                FinanceAccountMonthPolicy.monthContaining(normalizedTo);
        OffsetDateTime normalizedFrom = fromDate == null
                ? defaultMonth.startInclusive()
                : fromDate;

        if (normalizedTo.isBefore(normalizedFrom)) {
            throw com.lifebalance.finance.error.FinanceExceptions.invalidTransaction("Summary period is invalid");
        }

        FinanceAccountMonthPolicy.MonthRange accountMonths = fromDate == null
                ? defaultMonth
                : FinanceAccountMonthPolicy.monthsCovering(normalizedFrom, normalizedTo);

        BigDecimal totalIncome = transactionRepository.sumPostedAmount(
                ownerId,
                FinanceTransactionType.INCOME,
                normalizedCurrency,
                normalizedFrom,
                normalizedTo,
                null
        );
        BigDecimal totalExpense = transactionRepository.sumPostedAmount(
                ownerId,
                FinanceTransactionType.EXPENSE,
                normalizedCurrency,
                normalizedFrom,
                normalizedTo,
                null
        );
        BigDecimal mainPoolBalance = accountRepository.sumActiveBalanceByType(
                ownerId,
                normalizedCurrency,
                FinanceAccountType.MAIN_POOL
        );
        BigDecimal totalJarBalance = accountRepository.sumActiveBalanceByType(
                ownerId,
                normalizedCurrency,
                FinanceAccountType.JAR,
                accountMonths.startInclusive(),
                accountMonths.endExclusive()
        );
        BigDecimal totalBalance = mainPoolBalance.add(totalJarBalance);
        BigDecimal openingBalance = accountRepository.sumActiveOpeningBalanceByType(
                ownerId,
                normalizedCurrency,
                FinanceAccountType.MAIN_POOL
        );

        if (normalizedFrom.isAfter(EARLIEST_TRANSACTION_DATE)) {
            OffsetDateTime beforePeriod = normalizedFrom.minusNanos(1);
            BigDecimal incomeBeforePeriod = transactionRepository.sumPostedAmount(
                    ownerId,
                    FinanceTransactionType.INCOME,
                    normalizedCurrency,
                    EARLIEST_TRANSACTION_DATE,
                    beforePeriod,
                    null
            );
            BigDecimal expenseBeforePeriod = transactionRepository.sumPostedAmount(
                    ownerId,
                    FinanceTransactionType.EXPENSE,
                    normalizedCurrency,
                    EARLIEST_TRANSACTION_DATE,
                    beforePeriod,
                    null
            );
            openingBalance = openingBalance.add(incomeBeforePeriod).subtract(expenseBeforePeriod);
        }

        List<FinanceMonthlyJarSettlementSummaryResponse> monthlySettlements = summarizeSettlements(
                settlementRepository
                        .findByOwnerIdAndCurrencyCodeAndPeriodStartBetweenOrderByPeriodStartAsc(
                                ownerId,
                                normalizedCurrency,
                                YearMonth.from(accountMonths.startInclusive()).atDay(1),
                                YearMonth.from(accountMonths.endExclusive().minusDays(1)).atDay(1)
                        )
        );

        return new FinanceSummaryResponse(
                ownerId,
                normalizedCurrency,
                normalizedFrom,
                normalizedTo,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                mainPoolBalance,
                totalJarBalance,
                totalBalance,
                openingBalance,
                monthlySettlements
        );
    }

    private static List<FinanceMonthlyJarSettlementSummaryResponse> summarizeSettlements(
            List<FinanceMonthlyJarSettlement> settlements
    ) {
        Map<YearMonth, SettlementTotals> totalsByMonth = new LinkedHashMap<>();
        for (FinanceMonthlyJarSettlement settlement : settlements) {
            YearMonth period = YearMonth.from(settlement.getPeriodStart());
            totalsByMonth.computeIfAbsent(period, ignored -> new SettlementTotals()).add(settlement);
        }

        List<FinanceMonthlyJarSettlementSummaryResponse> summaries = new ArrayList<>();
        totalsByMonth.forEach((period, totals) -> summaries.add(new FinanceMonthlyJarSettlementSummaryResponse(
                period.toString(),
                totals.allocatedAmount,
                totals.actualExpenseAmount,
                totals.closingBalance,
                totals.returnedAmount,
                totals.coveredDeficitAmount,
                totals.varianceAmount,
                totals.settledJarCount
        )));
        return List.copyOf(summaries);
    }

    private static final class SettlementTotals {
        private BigDecimal allocatedAmount = BigDecimal.ZERO;
        private BigDecimal actualExpenseAmount = BigDecimal.ZERO;
        private BigDecimal closingBalance = BigDecimal.ZERO;
        private BigDecimal returnedAmount = BigDecimal.ZERO;
        private BigDecimal coveredDeficitAmount = BigDecimal.ZERO;
        private BigDecimal varianceAmount = BigDecimal.ZERO;
        private int settledJarCount;

        private void add(FinanceMonthlyJarSettlement settlement) {
            allocatedAmount = allocatedAmount.add(settlement.getAllocatedAmount());
            actualExpenseAmount = actualExpenseAmount.add(settlement.getActualExpenseAmount());
            closingBalance = closingBalance.add(settlement.getClosingBalance());
            if (settlement.getClosingBalance().signum() >= 0) {
                returnedAmount = returnedAmount.add(settlement.getTransferredAmount());
            } else {
                coveredDeficitAmount = coveredDeficitAmount.add(settlement.getTransferredAmount());
            }
            varianceAmount = varianceAmount.add(settlement.getVarianceAmount());
            settledJarCount++;
        }
    }
}
