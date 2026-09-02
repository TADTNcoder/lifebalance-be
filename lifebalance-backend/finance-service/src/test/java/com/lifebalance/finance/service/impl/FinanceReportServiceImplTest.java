package com.lifebalance.finance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceMonthlyJarSettlement;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.dto.FinanceSummaryResponse;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceMonthlyJarSettlementRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinanceReportServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final OffsetDateTime PERIOD_START = OffsetDateTime.parse("2026-08-01T00:00:00Z");
    private static final OffsetDateTime PERIOD_END = OffsetDateTime.parse("2026-08-31T23:59:59Z");
    private static final OffsetDateTime ACCOUNT_MONTH_START = OffsetDateTime.parse("2026-08-01T00:00:00+07:00");
    private static final OffsetDateTime ACCOUNT_MONTH_END = OffsetDateTime.parse("2026-10-01T00:00:00+07:00");
    private static final OffsetDateTime EARLIEST_TRANSACTION_DATE =
            OffsetDateTime.parse("0001-01-01T00:00:00Z");

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private FinanceAccountRepository accountRepository;

    @Mock
    private FinanceMonthlyJarSettlementRepository settlementRepository;

    @Test
    void summaryUsesOpeningBalanceBeforeSelectedPeriodAndCurrentActiveBalance() {
        when(accountRepository.sumActiveOpeningBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.MAIN_POOL))
                .thenReturn(new BigDecimal("1000.00"));
        when(accountRepository.sumActiveBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.MAIN_POOL))
                .thenReturn(new BigDecimal("750.00"));
        when(accountRepository.sumActiveBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.JAR, ACCOUNT_MONTH_START, ACCOUNT_MONTH_END))
                .thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumPostedAmount(
                eq(OWNER_ID), eq(FinanceTransactionType.INCOME), eq("VND"),
                eq(PERIOD_START), eq(PERIOD_END), eq(null)))
                .thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumPostedAmount(
                eq(OWNER_ID), eq(FinanceTransactionType.EXPENSE), eq("VND"),
                eq(PERIOD_START), eq(PERIOD_END), eq(null)))
                .thenReturn(new BigDecimal("250.00"));
        when(transactionRepository.sumPostedAmount(
                eq(OWNER_ID), eq(FinanceTransactionType.INCOME), eq("VND"),
                eq(EARLIEST_TRANSACTION_DATE),
                eq(PERIOD_START.minusNanos(1)), eq(null)))
                .thenReturn(new BigDecimal("300.00"));
        when(transactionRepository.sumPostedAmount(
                eq(OWNER_ID), eq(FinanceTransactionType.EXPENSE), eq("VND"),
                eq(EARLIEST_TRANSACTION_DATE),
                eq(PERIOD_START.minusNanos(1)), eq(null)))
                .thenReturn(new BigDecimal("100.00"));
        FinanceAccount mainPool = FinanceAccount.create(
                OWNER_ID, OWNER_ID, "Ví tổng", FinanceAccountType.MAIN_POOL, "VND", BigDecimal.ZERO);
        FinanceAccount jar = FinanceAccount.create(
                OWNER_ID, OWNER_ID, "Hũ sinh hoạt", FinanceAccountType.JAR, "VND", BigDecimal.ZERO);
        FinanceMonthlyJarSettlement firstSettlement = FinanceMonthlyJarSettlement.create(
                OWNER_ID,
                LocalDate.of(2026, 8, 1),
                "VND",
                mainPool,
                jar,
                null,
                new BigDecimal("1000.00"),
                new BigDecimal("600.00"),
                new BigDecimal("400.00"),
                new BigDecimal("400.00"),
                new BigDecimal("-400.00"),
                PERIOD_END
        );
        FinanceMonthlyJarSettlement secondSettlement = FinanceMonthlyJarSettlement.create(
                OWNER_ID,
                LocalDate.of(2026, 8, 1),
                "VND",
                mainPool,
                jar,
                null,
                new BigDecimal("500.00"),
                new BigDecimal("700.00"),
                new BigDecimal("-200.00"),
                new BigDecimal("200.00"),
                new BigDecimal("200.00"),
                PERIOD_END
        );
        when(settlementRepository
                .findByOwnerIdAndCurrencyCodeAndPeriodStartBetweenOrderByPeriodStartAsc(
                        OWNER_ID,
                        "VND",
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 9, 1)
                ))
                .thenReturn(List.of(firstSettlement, secondSettlement));

        FinanceSummaryResponse summary = new FinanceReportServiceImpl(
                transactionRepository,
                accountRepository,
                settlementRepository
        ).getSummary(OWNER_ID, " vnd ", PERIOD_START, PERIOD_END);

        assertThat(summary.totalIncome()).isEqualByComparingTo("500.00");
        assertThat(summary.totalExpense()).isEqualByComparingTo("250.00");
        assertThat(summary.netCashflow()).isEqualByComparingTo("250.00");
        assertThat(summary.mainPoolBalance()).isEqualByComparingTo("750.00");
        assertThat(summary.totalJarBalance()).isEqualByComparingTo("500.00");
        assertThat(summary.totalAccountBalance()).isEqualByComparingTo("1250.00");
        assertThat(summary.openingAccountBalance()).isEqualByComparingTo("1200.00");
        assertThat(summary.monthlyJarSettlements()).singleElement().satisfies(settlement -> {
            assertThat(settlement.period()).isEqualTo("2026-08");
            assertThat(settlement.allocatedAmount()).isEqualByComparingTo("1500.00");
            assertThat(settlement.actualExpenseAmount()).isEqualByComparingTo("1300.00");
            assertThat(settlement.returnedAmount()).isEqualByComparingTo("400.00");
            assertThat(settlement.coveredDeficitAmount()).isEqualByComparingTo("200.00");
            assertThat(settlement.varianceAmount()).isEqualByComparingTo("-200.00");
            assertThat(settlement.settledJarCount()).isEqualTo(2);
        });
    }

    @Test
    void summaryWithoutFromDateUsesTheSameMonthForTransactionsAndAccounts() {
        OffsetDateTime toDate = OffsetDateTime.parse("2026-08-31T16:59:59Z");
        OffsetDateTime monthStart = OffsetDateTime.parse("2026-08-01T00:00:00+07:00");
        OffsetDateTime monthEnd = OffsetDateTime.parse("2026-09-01T00:00:00+07:00");
        when(transactionRepository.sumPostedAmount(
                OWNER_ID, FinanceTransactionType.INCOME, "VND", monthStart, toDate, null))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumPostedAmount(
                OWNER_ID, FinanceTransactionType.EXPENSE, "VND", monthStart, toDate, null))
                .thenReturn(BigDecimal.ZERO);
        when(accountRepository.sumActiveBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.MAIN_POOL))
                .thenReturn(BigDecimal.ZERO);
        when(accountRepository.sumActiveBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.JAR, monthStart, monthEnd))
                .thenReturn(BigDecimal.ZERO);
        when(accountRepository.sumActiveOpeningBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.MAIN_POOL))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumPostedAmount(
                OWNER_ID, FinanceTransactionType.INCOME, "VND",
                EARLIEST_TRANSACTION_DATE, monthStart.minusNanos(1), null))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumPostedAmount(
                OWNER_ID, FinanceTransactionType.EXPENSE, "VND",
                EARLIEST_TRANSACTION_DATE, monthStart.minusNanos(1), null))
                .thenReturn(BigDecimal.ZERO);
        when(settlementRepository
                .findByOwnerIdAndCurrencyCodeAndPeriodStartBetweenOrderByPeriodStartAsc(
                        OWNER_ID, "VND", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1)))
                .thenReturn(List.of());

        FinanceSummaryResponse summary = new FinanceReportServiceImpl(
                transactionRepository,
                accountRepository,
                settlementRepository
        ).getSummary(OWNER_ID, "VND", null, toDate);

        assertThat(summary.fromDate()).isEqualTo(monthStart);
        verify(transactionRepository).sumPostedAmount(
                OWNER_ID, FinanceTransactionType.INCOME, "VND", monthStart, toDate, null);
        verify(accountRepository).sumActiveBalanceByType(
                OWNER_ID, "VND", FinanceAccountType.MAIN_POOL);
    }
}
