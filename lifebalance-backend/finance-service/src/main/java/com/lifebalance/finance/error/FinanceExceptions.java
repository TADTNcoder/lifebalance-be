package com.lifebalance.finance.error;

import com.lifebalance.common.error.AppException;
import com.lifebalance.finance.domain.FinanceAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public final class FinanceExceptions {

    private FinanceExceptions() {
    }

    public static AppException accountNotFound(UUID accountId) {
        return notFound(FinanceErrorCode.FINANCE_ACCOUNT_NOT_FOUND, "Finance account not found",
                Map.of("accountId", String.valueOf(accountId)));
    }

    public static AppException accountAlreadyExists(String name) {
        return conflict(FinanceErrorCode.FINANCE_ACCOUNT_ALREADY_EXISTS, "Finance account already exists",
                Map.of("name", String.valueOf(name)));
    }

    public static AppException accountNotActive(UUID accountId) {
        return conflict(FinanceErrorCode.FINANCE_ACCOUNT_NOT_ACTIVE, "Finance account is not active",
                Map.of("accountId", String.valueOf(accountId)));
    }

    public static AppException invalidAccount(String reason) {
        return conflict(FinanceErrorCode.FINANCE_ACCOUNT_INVALID, "Finance account is invalid",
                Map.of("reason", reason));
    }

    public static AppException categoryNotFound(UUID categoryId) {
        return notFound(FinanceErrorCode.FINANCE_CATEGORY_NOT_FOUND, "Finance category not found",
                Map.of("categoryId", String.valueOf(categoryId)));
    }

    public static AppException categoryAlreadyExists(String name) {
        return conflict(FinanceErrorCode.FINANCE_CATEGORY_ALREADY_EXISTS, "Finance category already exists",
                Map.of("name", String.valueOf(name)));
    }

    public static AppException categoryTypeMismatch(UUID categoryId, String expectedType, String actualType) {
        return conflict(FinanceErrorCode.FINANCE_CATEGORY_TYPE_MISMATCH, "Finance category type is not valid for transaction",
                Map.of(
                        "categoryId", String.valueOf(categoryId),
                        "expectedType", expectedType,
                        "actualType", actualType
                ));
    }

    public static AppException transactionNotFound(UUID transactionId) {
        return notFound(FinanceErrorCode.FINANCE_TRANSACTION_NOT_FOUND, "Financial transaction not found",
                Map.of("transactionId", String.valueOf(transactionId)));
    }

    public static AppException invalidTransaction(String reason) {
        return badRequest(FinanceErrorCode.FINANCE_TRANSACTION_INVALID, "Financial transaction is invalid",
                Map.of("reason", reason));
    }

    public static AppException transactionNotPosted(UUID transactionId) {
        return conflict(FinanceErrorCode.FINANCE_TRANSACTION_NOT_POSTED, "Financial transaction is not posted",
                Map.of("transactionId", String.valueOf(transactionId)));
    }

    public static AppException insufficientBalance(FinanceAccount account, BigDecimal amount) {
        return conflict(
                FinanceErrorCode.FINANCE_INSUFFICIENT_BALANCE,
                "Finance account balance is insufficient",
                Map.of(
                        "accountId", String.valueOf(account == null ? null : account.getId()),
                        "accountName", String.valueOf(account == null ? null : account.getName()),
                        "availableBalance", String.valueOf(account == null ? null : account.getCurrentBalance()),
                        "requestedAmount", String.valueOf(amount),
                        "currencyCode", String.valueOf(account == null ? null : account.getCurrencyCode())
                ));
    }

    public static AppException monthlySalaryAlreadyExists(UUID taskId, String salaryPeriod) {
        return conflict(
                FinanceErrorCode.FINANCE_MONTHLY_SALARY_ALREADY_EXISTS,
                "Monthly salary already exists for task and period",
                Map.of(
                        "taskId", String.valueOf(taskId),
                        "salaryPeriod", String.valueOf(salaryPeriod)
                ));
    }

    public static AppException budgetNotFound(UUID budgetId) {
        return notFound(FinanceErrorCode.FINANCE_BUDGET_NOT_FOUND, "Finance budget not found",
                Map.of("budgetId", String.valueOf(budgetId)));
    }

    public static AppException invalidBudget(String reason) {
        return badRequest(FinanceErrorCode.FINANCE_BUDGET_INVALID, "Finance budget is invalid",
                Map.of("reason", reason));
    }

    public static AppException budgetAlreadyExists(UUID categoryId, LocalDate start, LocalDate end) {
        return conflict(FinanceErrorCode.FINANCE_BUDGET_ALREADY_EXISTS, "Finance budget already exists for period",
                Map.of(
                        "categoryId", String.valueOf(categoryId),
                        "periodStart", String.valueOf(start),
                        "periodEnd", String.valueOf(end)
                ));
    }

    public static AppException recurringRuleNotFound(UUID ruleId) {
        return notFound(FinanceErrorCode.FINANCE_RECURRING_RULE_NOT_FOUND, "Recurring transaction rule not found",
                Map.of("ruleId", String.valueOf(ruleId)));
    }

    public static AppException invalidRecurringRule(String reason) {
        return badRequest(FinanceErrorCode.FINANCE_RECURRING_RULE_INVALID, "Recurring transaction rule is invalid",
                Map.of("reason", reason));
    }

    public static AppException currencyMismatch(String expected, String actual) {
        return conflict(FinanceErrorCode.FINANCE_CURRENCY_MISMATCH, "Currency is not consistent",
                Map.of("expectedCurrency", String.valueOf(expected), "actualCurrency", String.valueOf(actual)));
    }

    public static AppException invalidCurrency(String currencyCode) {
        return badRequest(FinanceErrorCode.FINANCE_INVALID_CURRENCY, "Currency code is invalid",
                Map.of("currencyCode", String.valueOf(currencyCode)));
    }

    public static AppException invalidPeriod(LocalDate start, LocalDate end) {
        return badRequest(FinanceErrorCode.FINANCE_INVALID_PERIOD, "Finance period is invalid",
                Map.of("periodStart", String.valueOf(start), "periodEnd", String.valueOf(end)));
    }

    public static AppException amountPrecisionExceeded(BigDecimal amount) {
        return badRequest(FinanceErrorCode.FINANCE_TRANSACTION_INVALID, "Amount precision is invalid",
                Map.of("amount", String.valueOf(amount)));
    }

    private static AppException notFound(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.NOT_FOUND, details);
    }

    private static AppException conflict(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.CONFLICT, details);
    }

    private static AppException badRequest(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.BAD_REQUEST, details);
    }
}
