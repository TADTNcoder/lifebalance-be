package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryStatus;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionStatus;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.FinancialTransaction;
import com.lifebalance.finance.dto.CreateTransactionRequest;
import com.lifebalance.finance.dto.TransactionResponse;
import com.lifebalance.finance.dto.UpdateTransactionRequest;
import com.lifebalance.finance.dto.VoidTransactionRequest;
import com.lifebalance.finance.error.FinanceExceptions;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import com.lifebalance.finance.service.FinancialTransactionService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialTransactionServiceImpl implements FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinanceAccountRepository accountRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final FinanceHistoryRecorder historyRecorder;

    public FinancialTransactionServiceImpl(
            FinancialTransactionRepository transactionRepository,
            FinanceAccountRepository accountRepository,
            FinanceCategoryRepository categoryRepository,
            FinanceHistoryRecorder historyRecorder
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.historyRecorder = historyRecorder;
    }

    @Override
    @Transactional
    public TransactionResponse create(UUID ownerId, CreateTransactionRequest request) {
        TransactionParts parts = resolveParts(
                ownerId,
                request.transactionType(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.categoryId(),
                request.amount(),
                request.currencyCode(),
                true
        );

        applyImpact(parts.transactionType(), parts.sourceAccount(), parts.destinationAccount(), parts.amount());

        FinancialTransaction transaction = FinancialTransaction.post(
                ownerId,
                ownerId,
                parts.transactionType(),
                parts.sourceAccount(),
                parts.destinationAccount(),
                parts.category(),
                parts.amount(),
                parts.currencyCode(),
                request.transactionDate(),
                request.description(),
                request.taskId(),
                request.capitalCycleId(),
                request.capitalAllocationId()
        );
        transaction = transactionRepository.save(transaction);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.TRANSACTION_CREATED,
                FinanceReferenceType.FINANCIAL_TRANSACTION,
                transaction.getId(),
                request.reason(),
                null,
                snapshot(transaction)
        );

        return FinanceMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse update(UUID ownerId, UUID transactionId, UpdateTransactionRequest request) {
        FinancialTransaction transaction = getPostedTransaction(ownerId, transactionId);
        String oldValue = snapshot(transaction);

        Map<UUID, FinanceAccount> oldAccounts = lockAccounts(
                ownerId,
                accountId(transaction.getSourceAccount()),
                accountId(transaction.getDestinationAccount())
        );
        reverseImpact(
                transaction.getTransactionType(),
                accountById(oldAccounts, transaction.getSourceAccount()),
                accountById(oldAccounts, transaction.getDestinationAccount()),
                transaction.getAmount()
        );

        TransactionParts parts = resolveParts(
                ownerId,
                request.transactionType(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.categoryId(),
                request.amount(),
                request.currencyCode(),
                true
        );
        applyImpact(parts.transactionType(), parts.sourceAccount(), parts.destinationAccount(), parts.amount());

        transaction.replaceWith(
                ownerId,
                parts.transactionType(),
                parts.sourceAccount(),
                parts.destinationAccount(),
                parts.category(),
                parts.amount(),
                parts.currencyCode(),
                request.transactionDate(),
                request.description(),
                request.taskId(),
                request.capitalCycleId(),
                request.capitalAllocationId()
        );
        transaction = transactionRepository.save(transaction);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.TRANSACTION_UPDATED,
                FinanceReferenceType.FINANCIAL_TRANSACTION,
                transaction.getId(),
                request.reason(),
                oldValue,
                snapshot(transaction)
        );

        return FinanceMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse voidTransaction(UUID ownerId, UUID transactionId, VoidTransactionRequest request) {
        FinancialTransaction transaction = getPostedTransaction(ownerId, transactionId);
        String oldValue = snapshot(transaction);

        Map<UUID, FinanceAccount> accounts = lockAccounts(
                ownerId,
                accountId(transaction.getSourceAccount()),
                accountId(transaction.getDestinationAccount())
        );
        reverseImpact(
                transaction.getTransactionType(),
                accountById(accounts, transaction.getSourceAccount()),
                accountById(accounts, transaction.getDestinationAccount()),
                transaction.getAmount()
        );

        transaction.voidTransaction(ownerId, request.reason());
        transaction = transactionRepository.save(transaction);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.TRANSACTION_VOIDED,
                FinanceReferenceType.FINANCIAL_TRANSACTION,
                transaction.getId(),
                request.reason(),
                oldValue,
                snapshot(transaction)
        );

        return FinanceMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID ownerId, UUID transactionId) {
        return FinanceMapper.toTransactionResponse(transactionRepository
                .findDetailedByIdAndOwnerId(transactionId, ownerId)
                .orElseThrow(() -> FinanceExceptions.transactionNotFound(transactionId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            UUID ownerId,
            FinanceTransactionType type,
            FinanceTransactionStatus status,
            UUID accountId,
            UUID categoryId,
            UUID taskId,
            UUID capitalCycleId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Pageable pageable
    ) {
        return transactionRepository
                .search(ownerId, type, status, accountId, categoryId, taskId, capitalCycleId, fromDate, toDate, pageable)
                .map(FinanceMapper::toTransactionResponse);
    }

    private FinancialTransaction getPostedTransaction(UUID ownerId, UUID transactionId) {
        FinancialTransaction transaction = transactionRepository
                .findDetailedByIdAndOwnerId(transactionId, ownerId)
                .orElseThrow(() -> FinanceExceptions.transactionNotFound(transactionId));

        if (!transaction.isPosted()) {
            throw FinanceExceptions.transactionNotPosted(transactionId);
        }

        return transaction;
    }

    private TransactionParts resolveParts(
            UUID ownerId,
            FinanceTransactionType transactionType,
            UUID sourceAccountId,
            UUID destinationAccountId,
            UUID categoryId,
            BigDecimal amount,
            String requestedCurrency,
            boolean requireActiveAccounts
    ) {
        FinanceSupport.validateTransactionShape(transactionType, sourceAccountId, destinationAccountId);

        String currencyCode = FinanceSupport.normalizeCurrency(requestedCurrency);
        BigDecimal normalizedAmount = FinanceSupport.normalizeAmount(amount);
        Map<UUID, FinanceAccount> lockedAccounts = lockAccounts(ownerId, sourceAccountId, destinationAccountId);
        FinanceAccount sourceAccount = lockedAccounts.get(sourceAccountId);
        FinanceAccount destinationAccount = lockedAccounts.get(destinationAccountId);

        if (requireActiveAccounts) {
            if (sourceAccount != null) {
                FinanceSupport.ensureAccountActive(sourceAccount);
            }
            if (destinationAccount != null) {
                FinanceSupport.ensureAccountActive(destinationAccount);
            }
        }
        if (sourceAccount != null) {
            FinanceSupport.ensureAccountCurrency(sourceAccount, currencyCode);
        }
        if (destinationAccount != null) {
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
            throw FinanceExceptions.invalidTransaction("Transfer transactions must not use income or expense category");
        }
        if (transactionType == FinanceTransactionType.INCOME) {
            FinanceSupport.ensureCategoryType(category, FinanceCategoryType.INCOME);
        }
        if (transactionType == FinanceTransactionType.EXPENSE) {
            FinanceSupport.ensureCategoryType(category, FinanceCategoryType.EXPENSE);
        }

        return new TransactionParts(
                transactionType,
                sourceAccount,
                destinationAccount,
                category,
                normalizedAmount,
                currencyCode
        );
    }

    private Map<UUID, FinanceAccount> lockAccounts(
            UUID ownerId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        Map<UUID, FinanceAccount> accounts = new HashMap<>();

        java.util.stream.Stream.of(sourceAccountId, destinationAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(UUID::toString))
                .forEach(accountId -> accounts.put(
                        accountId,
                        accountRepository.findByIdAndOwnerIdForUpdate(accountId, ownerId)
                                .orElseThrow(() -> FinanceExceptions.accountNotFound(accountId))
                ));

        return accounts;
    }

    private void applyImpact(
            FinanceTransactionType type,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            BigDecimal amount
    ) {
        switch (type) {
            case INCOME -> destinationAccount.credit(amount);
            case EXPENSE -> sourceAccount.debit(amount);
            case TRANSFER -> {
                sourceAccount.debit(amount);
                destinationAccount.credit(amount);
            }
        }
    }

    private void reverseImpact(
            FinanceTransactionType type,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            BigDecimal amount
    ) {
        switch (type) {
            case INCOME -> destinationAccount.debit(amount);
            case EXPENSE -> sourceAccount.credit(amount);
            case TRANSFER -> {
                sourceAccount.credit(amount);
                destinationAccount.debit(amount);
            }
        }
    }

    private static UUID accountId(FinanceAccount account) {
        return account == null ? null : account.getId();
    }

    private static FinanceAccount accountById(Map<UUID, FinanceAccount> accounts, FinanceAccount account) {
        return account == null ? null : accounts.get(account.getId());
    }

    private static String snapshot(FinancialTransaction transaction) {
        return "type=" + transaction.getTransactionType()
                + ";status=" + transaction.getStatus()
                + ";sourceAccountId=" + accountId(transaction.getSourceAccount())
                + ";destinationAccountId=" + accountId(transaction.getDestinationAccount())
                + ";categoryId=" + (transaction.getCategory() == null ? null : transaction.getCategory().getId())
                + ";amount=" + transaction.getAmount()
                + ";currency=" + transaction.getCurrencyCode()
                + ";transactionDate=" + transaction.getTransactionDate()
                + ";taskId=" + transaction.getTaskId()
                + ";capitalCycleId=" + transaction.getCapitalCycleId()
                + ";capitalAllocationId=" + transaction.getCapitalAllocationId();
    }

    private record TransactionParts(
            FinanceTransactionType transactionType,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            FinanceCategory category,
            BigDecimal amount,
            String currencyCode
    ) {
    }
}
