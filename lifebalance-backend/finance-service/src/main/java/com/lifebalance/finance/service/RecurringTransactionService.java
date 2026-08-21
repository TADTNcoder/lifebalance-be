package com.lifebalance.finance.service;

import com.lifebalance.finance.domain.RecurringTransactionStatus;
import com.lifebalance.finance.dto.CreateRecurringTransactionRequest;
import com.lifebalance.finance.dto.RecurringTransactionResponse;
import com.lifebalance.finance.dto.UpdateRecurringTransactionRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecurringTransactionService {

    RecurringTransactionResponse create(UUID ownerId, CreateRecurringTransactionRequest request);

    RecurringTransactionResponse update(UUID ownerId, UUID ruleId, UpdateRecurringTransactionRequest request);

    RecurringTransactionResponse pause(UUID ownerId, UUID ruleId, String reason);

    RecurringTransactionResponse resume(UUID ownerId, UUID ruleId, String reason);

    RecurringTransactionResponse end(UUID ownerId, UUID ruleId, String reason);

    Page<RecurringTransactionResponse> getRules(
            UUID ownerId,
            RecurringTransactionStatus status,
            LocalDate dueOnOrBefore,
            Pageable pageable);
}
