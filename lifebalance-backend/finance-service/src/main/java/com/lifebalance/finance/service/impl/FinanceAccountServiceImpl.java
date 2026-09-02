package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.dto.CreateFinanceAccountRequest;
import com.lifebalance.finance.dto.FinanceAccountResponse;
import com.lifebalance.finance.dto.UpdateFinanceAccountRequest;
import com.lifebalance.finance.error.FinanceExceptions;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.service.FinanceAccountService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceAccountServiceImpl implements FinanceAccountService {

    private final FinanceAccountRepository financeAccountRepository;
    private final FinanceHistoryRecorder historyRecorder;

    public FinanceAccountServiceImpl(
            FinanceAccountRepository financeAccountRepository,
            FinanceHistoryRecorder historyRecorder
    ) {
        this.financeAccountRepository = financeAccountRepository;
        this.historyRecorder = historyRecorder;
    }

    @Override
    @Transactional
    public FinanceAccountResponse create(UUID ownerId, CreateFinanceAccountRequest request) {
        String currencyCode = FinanceSupport.normalizeCurrency(request.currencyCode());
        BigDecimal openingBalance = FinanceSupport.normalizeAmount(request.openingBalance());
        String name = request.name().trim();
        FinanceAccountMonthPolicy.MonthRange effectiveMonth = FinanceAccountMonthPolicy.currentMonth();

        validateAccountStructure(
                ownerId,
                request.accountType(),
                currencyCode,
                openingBalance
        );

        if (financeAccountRepository.existsNameInCreatedPeriod(
                ownerId,
                name,
                FinanceAccountStatus.ACTIVE,
                effectiveMonth.startInclusive(),
                effectiveMonth.endExclusive()
        )) {
            throw FinanceExceptions.accountAlreadyExists(name);
        }

        FinanceAccount account = FinanceAccount.create(
                ownerId,
                ownerId,
                name,
                request.accountType(),
                currencyCode,
                openingBalance
        );
        try {
            account = financeAccountRepository.save(account);
            if (request.accountType() == FinanceAccountType.MAIN_POOL) {
                // Force the lifetime unique index to run inside this service call,
                // so a concurrent create is returned as a domain conflict.
                financeAccountRepository.flush();
            }
        } catch (DataIntegrityViolationException exception) {
            if (request.accountType() == FinanceAccountType.MAIN_POOL) {
                throw FinanceExceptions.invalidAccount(
                        "Only one active main pool is allowed for a lifetime"
                );
            }
            throw exception;
        }

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.ACCOUNT_CREATED,
                FinanceReferenceType.FINANCE_ACCOUNT,
                account.getId(),
                null,
                null,
                snapshot(account)
        );

        return FinanceMapper.toAccountResponse(account);
    }

    @Override
    @Transactional
    public FinanceAccountResponse update(UUID ownerId, UUID accountId, UpdateFinanceAccountRequest request) {
        FinanceAccount account = getOwnedAccount(ownerId, accountId);
        String name = request.name().trim();
        FinanceAccountMonthPolicy.MonthRange accountMonth = account.getCreatedAt() == null
                ? FinanceAccountMonthPolicy.currentMonth()
                : FinanceAccountMonthPolicy.monthContaining(account.getCreatedAt());

        if (!account.getName().equalsIgnoreCase(name)
                && financeAccountRepository.existsNameInCreatedPeriod(
                ownerId,
                name,
                FinanceAccountStatus.ACTIVE,
                accountMonth.startInclusive(),
                accountMonth.endExclusive()
        )) {
            throw FinanceExceptions.accountAlreadyExists(name);
        }

        String oldValue = snapshot(account);
        account.updateDetails(ownerId, name);
        account = financeAccountRepository.save(account);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.ACCOUNT_UPDATED,
                FinanceReferenceType.FINANCE_ACCOUNT,
                account.getId(),
                request.reason(),
                oldValue,
                snapshot(account)
        );

        return FinanceMapper.toAccountResponse(account);
    }

    @Override
    @Transactional
    public FinanceAccountResponse archive(UUID ownerId, UUID accountId, String reason) {
        FinanceAccount account = getOwnedAccount(ownerId, accountId);
        if (account.getAccountType() == FinanceAccountType.MAIN_POOL) {
            throw FinanceExceptions.invalidAccount("Main pool cannot be archived");
        }
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw FinanceExceptions.invalidAccount("Jar balance must be zero before archiving");
        }
        String oldValue = snapshot(account);
        account.archive(ownerId);
        account = financeAccountRepository.save(account);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.ACCOUNT_ARCHIVED,
                FinanceReferenceType.FINANCE_ACCOUNT,
                account.getId(),
                reason,
                oldValue,
                snapshot(account)
        );

        return FinanceMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceAccountResponse getById(UUID ownerId, UUID accountId) {
        return FinanceMapper.toAccountResponse(getOwnedAccount(ownerId, accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinanceAccountResponse> getAccounts(
            UUID ownerId,
            FinanceAccountStatus status,
            String currencyCode,
            Pageable pageable
    ) {
        String normalizedCurrency = currencyCode == null || currencyCode.isBlank()
                ? null
                : FinanceSupport.normalizeCurrency(currencyCode);

        return financeAccountRepository.search(ownerId, status, normalizedCurrency, pageable)
                .map(FinanceMapper::toAccountResponse);
    }

    private FinanceAccount getOwnedAccount(UUID ownerId, UUID accountId) {
        return financeAccountRepository.findByIdAndOwnerId(accountId, ownerId)
                .orElseThrow(() -> FinanceExceptions.accountNotFound(accountId));
    }

    private void validateAccountStructure(
            UUID ownerId,
            FinanceAccountType accountType,
            String currencyCode,
            BigDecimal openingBalance
    ) {
        if (accountType == FinanceAccountType.MAIN_POOL) {
            if (financeAccountRepository.existsActiveMainPool(ownerId)) {
                throw FinanceExceptions.invalidAccount("Only one active main pool is allowed for a lifetime");
            }
            return;
        }

        FinanceAccount mainPool = financeAccountRepository
                .findFirstByOwnerIdAndAccountTypeAndStatusOrderByCreatedAtAscIdAsc(
                        ownerId,
                        FinanceAccountType.MAIN_POOL,
                        FinanceAccountStatus.ACTIVE
                )
                .orElseThrow(() -> FinanceExceptions.invalidAccount(
                        "Create the lifetime main pool before creating jars"
                ));

        if (!mainPool.getCurrencyCode().equals(currencyCode)) {
            throw FinanceExceptions.currencyMismatch(mainPool.getCurrencyCode(), currencyCode);
        }
        if (openingBalance.compareTo(BigDecimal.ZERO) != 0) {
            throw FinanceExceptions.invalidAccount("New jars must start at zero and be funded by a transfer");
        }
    }

    private static String snapshot(FinanceAccount account) {
        return "name=" + account.getName()
                + ";type=" + account.getAccountType()
                + ";currency=" + account.getCurrencyCode()
                + ";openingBalance=" + account.getOpeningBalance()
                + ";currentBalance=" + account.getCurrentBalance()
                + ";status=" + account.getStatus();
    }
}
