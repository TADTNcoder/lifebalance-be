package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.dto.FinanceSummaryResponse;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import com.lifebalance.finance.service.FinanceReportService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceReportServiceImpl implements FinanceReportService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinanceAccountRepository accountRepository;

    public FinanceReportServiceImpl(
            FinancialTransactionRepository transactionRepository,
            FinanceAccountRepository accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
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
        OffsetDateTime normalizedFrom = fromDate == null ? OffsetDateTime.parse("1970-01-01T00:00:00Z") : fromDate;
        OffsetDateTime normalizedTo = toDate == null ? OffsetDateTime.now() : toDate;

        if (normalizedTo.isBefore(normalizedFrom)) {
            throw com.lifebalance.finance.error.FinanceExceptions.invalidTransaction("Summary period is invalid");
        }

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
        BigDecimal totalBalance = accountRepository.sumActiveBalance(ownerId, normalizedCurrency);

        return new FinanceSummaryResponse(
                ownerId,
                normalizedCurrency,
                normalizedFrom,
                normalizedTo,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                totalBalance
        );
    }
}
