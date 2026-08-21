package com.lifebalance.finance.service;

import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.dto.CreateFinanceAccountRequest;
import com.lifebalance.finance.dto.FinanceAccountResponse;
import com.lifebalance.finance.dto.UpdateFinanceAccountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface FinanceAccountService {

    FinanceAccountResponse create(UUID ownerId, CreateFinanceAccountRequest request);

    FinanceAccountResponse update(UUID ownerId, UUID accountId, UpdateFinanceAccountRequest request);

    FinanceAccountResponse archive(UUID ownerId, UUID accountId, String reason);

    FinanceAccountResponse getById(UUID ownerId, UUID accountId);

    Page<FinanceAccountResponse> getAccounts(
            UUID ownerId,
            FinanceAccountStatus status,
            String currencyCode,
            Pageable pageable);
}
