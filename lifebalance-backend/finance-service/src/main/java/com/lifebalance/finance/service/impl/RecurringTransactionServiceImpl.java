package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.RecurrenceFrequency;
import com.lifebalance.finance.domain.RecurringTransactionRule;
import com.lifebalance.finance.domain.RecurringTransactionStatus;
import com.lifebalance.finance.dto.CreateRecurringTransactionRequest;
import com.lifebalance.finance.dto.RecurringTransactionResponse;
import com.lifebalance.finance.dto.UpdateRecurringTransactionRequest;
import com.lifebalance.finance.error.FinanceExceptions;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.repository.RecurringTransactionRuleRepository;
import com.lifebalance.finance.service.RecurringTransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRuleRepository recurringRuleRepository;
    private final FinanceAccountRepository accountRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final FinanceHistoryRecorder historyRecorder;

    public RecurringTransactionServiceImpl(
            RecurringTransactionRuleRepository recurringRuleRepository,
            FinanceAccountRepository accountRepository,
            FinanceCategoryRepository categoryRepository,
            FinanceHistoryRecorder historyRecorder
    ) {
        this.recurringRuleRepository = recurringRuleRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.historyRecorder = historyRecorder;
    }

    @Override
    @Transactional
    public RecurringTransactionResponse create(UUID ownerId, CreateRecurringTransactionRequest request) {
        RecurringParts parts = resolveParts(
                ownerId,
                request.transactionType(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.categoryId(),
                request.amount(),
                request.currencyCode()
        );
        LocalDate nextRunDate = request.nextRunDate() == null ? request.startsOn() : request.nextRunDate();
        validateSchedule(request.frequency(), request.intervalCount(), request.startsOn(), nextRunDate, request.endsOn());

        RecurringTransactionRule rule = RecurringTransactionRule.create(
                ownerId,
                ownerId,
                parts.transactionType(),
                parts.sourceAccount(),
                parts.destinationAccount(),
                parts.category(),
                parts.amount(),
                parts.currencyCode(),
                request.frequency(),
                request.intervalCount(),
                request.startsOn(),
                nextRunDate,
                request.endsOn(),
                request.description()
        );
        rule = recurringRuleRepository.save(rule);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.RECURRING_TRANSACTION_CREATED,
                FinanceReferenceType.RECURRING_TRANSACTION_RULE,
                rule.getId(),
                request.reason(),
                null,
                snapshot(rule)
        );

        return FinanceMapper.toRecurringResponse(rule);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(UUID ownerId, UUID ruleId, UpdateRecurringTransactionRequest request) {
        RecurringTransactionRule rule = getOwnedRule(ownerId, ruleId);
        if (rule.getStatus() == RecurringTransactionStatus.ENDED) {
            throw FinanceExceptions.invalidRecurringRule("Ended recurring transaction rule cannot be updated");
        }

        RecurringParts parts = resolveParts(
                ownerId,
                request.transactionType(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.categoryId(),
                request.amount(),
                request.currencyCode()
        );
        validateSchedule(request.frequency(), request.intervalCount(), request.startsOn(), request.nextRunDate(), request.endsOn());

        String oldValue = snapshot(rule);
        rule.updateDetails(
                ownerId,
                parts.transactionType(),
                parts.sourceAccount(),
                parts.destinationAccount(),
                parts.category(),
                parts.amount(),
                parts.currencyCode(),
                request.frequency(),
                request.intervalCount(),
                request.startsOn(),
                request.nextRunDate(),
                request.endsOn(),
                request.description()
        );
        rule = recurringRuleRepository.save(rule);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.RECURRING_TRANSACTION_UPDATED,
                FinanceReferenceType.RECURRING_TRANSACTION_RULE,
                rule.getId(),
                request.reason(),
                oldValue,
                snapshot(rule)
        );

        return FinanceMapper.toRecurringResponse(rule);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse pause(UUID ownerId, UUID ruleId, String reason) {
        RecurringTransactionRule rule = getOwnedRule(ownerId, ruleId);
        ensureCanPause(rule);
        String oldValue = snapshot(rule);
        rule.pause(ownerId);
        rule = recurringRuleRepository.save(rule);
        recordStateChange(ownerId, rule, FinanceHistoryActionType.RECURRING_TRANSACTION_PAUSED, reason, oldValue);
        return FinanceMapper.toRecurringResponse(rule);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse resume(UUID ownerId, UUID ruleId, String reason) {
        RecurringTransactionRule rule = getOwnedRule(ownerId, ruleId);
        ensureCanResume(rule);
        String oldValue = snapshot(rule);
        rule.resume(ownerId);
        rule = recurringRuleRepository.save(rule);
        recordStateChange(ownerId, rule, FinanceHistoryActionType.RECURRING_TRANSACTION_RESUMED, reason, oldValue);
        return FinanceMapper.toRecurringResponse(rule);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse end(UUID ownerId, UUID ruleId, String reason) {
        RecurringTransactionRule rule = getOwnedRule(ownerId, ruleId);
        ensureCanEnd(rule);
        String oldValue = snapshot(rule);
        rule.end(ownerId);
        rule = recurringRuleRepository.save(rule);
        recordStateChange(ownerId, rule, FinanceHistoryActionType.RECURRING_TRANSACTION_ENDED, reason, oldValue);
        return FinanceMapper.toRecurringResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecurringTransactionResponse> getRules(
            UUID ownerId,
            RecurringTransactionStatus status,
            LocalDate dueOnOrBefore,
            Pageable pageable
    ) {
        return recurringRuleRepository.search(ownerId, status, dueOnOrBefore, pageable)
                .map(FinanceMapper::toRecurringResponse);
    }

    private RecurringTransactionRule getOwnedRule(UUID ownerId, UUID ruleId) {
        return recurringRuleRepository.findByIdAndOwnerId(ruleId, ownerId)
                .orElseThrow(() -> FinanceExceptions.recurringRuleNotFound(ruleId));
    }

    private RecurringParts resolveParts(
            UUID ownerId,
            FinanceTransactionType transactionType,
            UUID sourceAccountId,
            UUID destinationAccountId,
            UUID categoryId,
            BigDecimal amount,
            String requestedCurrency
    ) {
        FinanceSupport.validateTransactionShape(transactionType, sourceAccountId, destinationAccountId);
        String currencyCode = FinanceSupport.normalizeCurrency(requestedCurrency);
        BigDecimal normalizedAmount = FinanceSupport.normalizeAmount(amount);

        FinanceAccount sourceAccount = sourceAccountId == null
                ? null
                : accountRepository.findByIdAndOwnerId(sourceAccountId, ownerId)
                .orElseThrow(() -> FinanceExceptions.accountNotFound(sourceAccountId));
        FinanceAccount destinationAccount = destinationAccountId == null
                ? null
                : accountRepository.findByIdAndOwnerId(destinationAccountId, ownerId)
                .orElseThrow(() -> FinanceExceptions.accountNotFound(destinationAccountId));

        if (sourceAccount != null) {
            FinanceSupport.ensureAccountActive(sourceAccount);
            FinanceSupport.ensureAccountCurrency(sourceAccount, currencyCode);
        }
        if (destinationAccount != null) {
            FinanceSupport.ensureAccountActive(destinationAccount);
            FinanceSupport.ensureAccountCurrency(destinationAccount, currencyCode);
        }

        FinanceCategory category = null;
        if (categoryId != null) {
            category = categoryRepository.findByIdAndOwnerId(categoryId, ownerId)
                    .orElseThrow(() -> FinanceExceptions.categoryNotFound(categoryId));
            if (!category.isActive()) {
                throw FinanceExceptions.categoryNotFound(categoryId);
            }
        }

        if (transactionType == FinanceTransactionType.TRANSFER && category != null) {
            throw FinanceExceptions.invalidRecurringRule("Transfer recurring rule must not use income or expense category");
        }
        if (transactionType == FinanceTransactionType.INCOME) {
            FinanceSupport.ensureCategoryType(category, FinanceCategoryType.INCOME);
        }
        if (transactionType == FinanceTransactionType.EXPENSE) {
            FinanceSupport.ensureCategoryType(category, FinanceCategoryType.EXPENSE);
        }

        return new RecurringParts(
                transactionType,
                sourceAccount,
                destinationAccount,
                category,
                normalizedAmount,
                currencyCode
        );
    }

    private static void validateSchedule(
            RecurrenceFrequency frequency,
            int intervalCount,
            LocalDate startsOn,
            LocalDate nextRunDate,
            LocalDate endsOn
    ) {
        if (frequency == null) {
            throw FinanceExceptions.invalidRecurringRule("Frequency is required");
        }
        if (intervalCount <= 0) {
            throw FinanceExceptions.invalidRecurringRule("Interval count must be greater than zero");
        }
        if (startsOn == null || nextRunDate == null) {
            throw FinanceExceptions.invalidRecurringRule("Start date and next run date are required");
        }
        if (nextRunDate.isBefore(startsOn)) {
            throw FinanceExceptions.invalidRecurringRule("Next run date cannot be before start date");
        }
        if (endsOn != null && endsOn.isBefore(startsOn)) {
            throw FinanceExceptions.invalidRecurringRule("End date cannot be before start date");
        }
    }

    private static void ensureCanPause(RecurringTransactionRule rule) {
        if (rule.getStatus() != RecurringTransactionStatus.ACTIVE) {
            throw FinanceExceptions.invalidRecurringRule("Only active recurring transaction rule can be paused");
        }
    }

    private static void ensureCanResume(RecurringTransactionRule rule) {
        if (rule.getStatus() != RecurringTransactionStatus.PAUSED) {
            throw FinanceExceptions.invalidRecurringRule("Only paused recurring transaction rule can be resumed");
        }
    }

    private static void ensureCanEnd(RecurringTransactionRule rule) {
        if (rule.getStatus() == RecurringTransactionStatus.ENDED) {
            throw FinanceExceptions.invalidRecurringRule("Recurring transaction rule is already ended");
        }
    }

    private void recordStateChange(
            UUID ownerId,
            RecurringTransactionRule rule,
            FinanceHistoryActionType actionType,
            String reason,
            String oldValue
    ) {
        historyRecorder.record(
                ownerId,
                ownerId,
                actionType,
                FinanceReferenceType.RECURRING_TRANSACTION_RULE,
                rule.getId(),
                reason,
                oldValue,
                snapshot(rule)
        );
    }

    private static String snapshot(RecurringTransactionRule rule) {
        return "type=" + rule.getTransactionType()
                + ";status=" + rule.getStatus()
                + ";sourceAccountId=" + accountId(rule.getSourceAccount())
                + ";destinationAccountId=" + accountId(rule.getDestinationAccount())
                + ";categoryId=" + (rule.getCategory() == null ? null : rule.getCategory().getId())
                + ";amount=" + rule.getAmount()
                + ";currency=" + rule.getCurrencyCode()
                + ";frequency=" + rule.getFrequency()
                + ";interval=" + rule.getIntervalCount()
                + ";startsOn=" + rule.getStartsOn()
                + ";nextRunDate=" + rule.getNextRunDate()
                + ";endsOn=" + rule.getEndsOn();
    }

    private static UUID accountId(FinanceAccount account) {
        return account == null ? null : account.getId();
    }

    private record RecurringParts(
            FinanceTransactionType transactionType,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            FinanceCategory category,
            BigDecimal amount,
            String currencyCode
    ) {
    }
}
