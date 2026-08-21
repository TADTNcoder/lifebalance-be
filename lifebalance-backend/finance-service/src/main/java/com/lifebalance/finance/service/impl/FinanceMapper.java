package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceBudget;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceHistory;
import com.lifebalance.finance.domain.FinancialTransaction;
import com.lifebalance.finance.domain.RecurringTransactionRule;
import com.lifebalance.finance.dto.BudgetResponse;
import com.lifebalance.finance.dto.FinanceAccountResponse;
import com.lifebalance.finance.dto.FinanceCategoryResponse;
import com.lifebalance.finance.dto.FinanceHistoryResponse;
import com.lifebalance.finance.dto.RecurringTransactionResponse;
import com.lifebalance.finance.dto.TransactionResponse;

final class FinanceMapper {

    private FinanceMapper() {
    }

    static FinanceAccountResponse toAccountResponse(FinanceAccount account) {
        return new FinanceAccountResponse(
                account.getId(),
                account.getOwnerId(),
                account.getName(),
                account.getAccountType(),
                account.getCurrencyCode(),
                account.getOpeningBalance(),
                account.getCurrentBalance(),
                account.getStatus(),
                account.getCreatedBy(),
                account.getUpdatedBy(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    static FinanceCategoryResponse toCategoryResponse(FinanceCategory category) {
        return new FinanceCategoryResponse(
                category.getId(),
                category.getOwnerId(),
                category.getName(),
                category.getCategoryType(),
                category.getColor(),
                category.getIcon(),
                category.getStatus(),
                category.getCreatedBy(),
                category.getUpdatedBy(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    static TransactionResponse toTransactionResponse(FinancialTransaction transaction) {
        FinanceAccount sourceAccount = transaction.getSourceAccount();
        FinanceAccount destinationAccount = transaction.getDestinationAccount();
        FinanceCategory category = transaction.getCategory();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getOwnerId(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                sourceAccount == null ? null : sourceAccount.getId(),
                sourceAccount == null ? null : sourceAccount.getName(),
                destinationAccount == null ? null : destinationAccount.getId(),
                destinationAccount == null ? null : destinationAccount.getName(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.getTaskId(),
                transaction.getCapitalCycleId(),
                transaction.getCapitalAllocationId(),
                transaction.getVoidedAt(),
                transaction.getVoidReason(),
                transaction.getCreatedBy(),
                transaction.getUpdatedBy(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    static BudgetResponse toBudgetResponse(FinanceBudget budget) {
        FinanceCategory category = budget.getCategory();

        return new BudgetResponse(
                budget.getId(),
                budget.getOwnerId(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                budget.getName(),
                budget.getPeriodStart(),
                budget.getPeriodEnd(),
                budget.getAmountLimit(),
                budget.getCurrencyCode(),
                budget.getAlertThresholdPercent(),
                budget.getStatus(),
                budget.getCreatedBy(),
                budget.getUpdatedBy(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    static RecurringTransactionResponse toRecurringResponse(RecurringTransactionRule rule) {
        FinanceAccount sourceAccount = rule.getSourceAccount();
        FinanceAccount destinationAccount = rule.getDestinationAccount();
        FinanceCategory category = rule.getCategory();

        return new RecurringTransactionResponse(
                rule.getId(),
                rule.getOwnerId(),
                rule.getTransactionType(),
                rule.getStatus(),
                sourceAccount == null ? null : sourceAccount.getId(),
                sourceAccount == null ? null : sourceAccount.getName(),
                destinationAccount == null ? null : destinationAccount.getId(),
                destinationAccount == null ? null : destinationAccount.getName(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                rule.getAmount(),
                rule.getCurrencyCode(),
                rule.getFrequency(),
                rule.getIntervalCount(),
                rule.getStartsOn(),
                rule.getNextRunDate(),
                rule.getEndsOn(),
                rule.getDescription(),
                rule.getCreatedBy(),
                rule.getUpdatedBy(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    static FinanceHistoryResponse toHistoryResponse(FinanceHistory history) {
        return new FinanceHistoryResponse(
                history.getId(),
                history.getOwnerId(),
                history.getActorId(),
                history.getActionType(),
                history.getReferenceType(),
                history.getReferenceId(),
                history.getReason(),
                history.getOldValue(),
                history.getNewValue(),
                history.getOccurredAt()
        );
    }
}
