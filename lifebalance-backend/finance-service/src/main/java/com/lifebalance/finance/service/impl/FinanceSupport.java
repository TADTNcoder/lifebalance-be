package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.error.FinanceExceptions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

final class FinanceSupport {

    static final int AMOUNT_SCALE = 4;
    static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private FinanceSupport() {
    }

    static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }

        try {
            return amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw FinanceExceptions.amountPrecisionExceeded(amount);
        }
    }

    static String normalizeCurrency(String currencyCode) {
        if (currencyCode == null) {
            throw FinanceExceptions.invalidCurrency(null);
        }

        String normalized = currencyCode.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("^[A-Z]{3}$")) {
            throw FinanceExceptions.invalidCurrency(currencyCode);
        }

        return normalized;
    }

    static void ensurePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart)) {
            throw FinanceExceptions.invalidPeriod(periodStart, periodEnd);
        }
    }

    static void ensureAccountActive(FinanceAccount account) {
        if (account == null || !account.isActive()) {
            throw FinanceExceptions.accountNotActive(account == null ? null : account.getId());
        }
    }

    static void ensureAccountCurrency(FinanceAccount account, String currencyCode) {
        if (!account.getCurrencyCode().equals(currencyCode)) {
            throw FinanceExceptions.currencyMismatch(account.getCurrencyCode(), currencyCode);
        }
    }

    static void ensureCategoryType(FinanceCategory category, FinanceCategoryType expectedType) {
        if (category == null) {
            return;
        }

        if (category.getCategoryType() != expectedType) {
            throw FinanceExceptions.categoryTypeMismatch(
                    category.getId(),
                    expectedType.name(),
                    category.getCategoryType().name()
            );
        }
    }

    static void validateTransactionShape(
            FinanceTransactionType transactionType,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        if (transactionType == null) {
            throw FinanceExceptions.invalidTransaction("Transaction type is required");
        }

        switch (transactionType) {
            case INCOME -> {
                if (sourceAccountId != null || destinationAccountId == null) {
                    throw FinanceExceptions.invalidTransaction("Income requires only destination account");
                }
            }
            case EXPENSE -> {
                if (sourceAccountId == null || destinationAccountId != null) {
                    throw FinanceExceptions.invalidTransaction("Expense requires only source account");
                }
            }
            case TRANSFER -> {
                if (sourceAccountId == null || destinationAccountId == null || sourceAccountId.equals(destinationAccountId)) {
                    throw FinanceExceptions.invalidTransaction("Transfer requires two different accounts");
                }
            }
        }
    }
}
