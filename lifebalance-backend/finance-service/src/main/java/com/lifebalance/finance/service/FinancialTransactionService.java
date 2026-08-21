package com.lifebalance.finance.service;

import com.lifebalance.finance.domain.FinanceTransactionStatus;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.dto.CreateTransactionRequest;
import com.lifebalance.finance.dto.TransactionResponse;
import com.lifebalance.finance.dto.UpdateTransactionRequest;
import com.lifebalance.finance.dto.VoidTransactionRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FinancialTransactionService {

    TransactionResponse create(UUID ownerId, CreateTransactionRequest request);

    TransactionResponse update(UUID ownerId, UUID transactionId, UpdateTransactionRequest request);

    TransactionResponse voidTransaction(UUID ownerId, UUID transactionId, VoidTransactionRequest request);

    TransactionResponse getById(UUID ownerId, UUID transactionId);

    Page<TransactionResponse> getTransactions(
            UUID ownerId,
            FinanceTransactionType type,
            FinanceTransactionStatus status,
            UUID accountId,
            UUID categoryId,
            UUID taskId,
            UUID capitalCycleId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Pageable pageable);
}
