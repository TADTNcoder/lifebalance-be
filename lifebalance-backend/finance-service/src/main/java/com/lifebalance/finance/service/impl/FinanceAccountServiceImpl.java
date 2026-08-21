package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountStatus;
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

        if (financeAccountRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                ownerId,
                name,
                FinanceAccountStatus.ACTIVE
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
        account = financeAccountRepository.save(account);

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

        if (!account.getName().equalsIgnoreCase(name)
                && financeAccountRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                ownerId,
                name,
                FinanceAccountStatus.ACTIVE
        )) {
            throw FinanceExceptions.accountAlreadyExists(name);
        }

        String oldValue = snapshot(account);
        account.updateDetails(ownerId, name, request.accountType());
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

    private static String snapshot(FinanceAccount account) {
        return "name=" + account.getName()
                + ";type=" + account.getAccountType()
                + ";currency=" + account.getCurrencyCode()
                + ";openingBalance=" + account.getOpeningBalance()
                + ";currentBalance=" + account.getCurrentBalance()
                + ";status=" + account.getStatus();
    }
}
