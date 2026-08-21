package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceHistory;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.repository.FinanceHistoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class FinanceHistoryRecorder {

    private final FinanceHistoryRepository financeHistoryRepository;

    FinanceHistoryRecorder(FinanceHistoryRepository financeHistoryRepository) {
        this.financeHistoryRepository = financeHistoryRepository;
    }

    void record(
            UUID ownerId,
            UUID actorId,
            FinanceHistoryActionType actionType,
            FinanceReferenceType referenceType,
            UUID referenceId,
            String reason,
            String oldValue,
            String newValue
    ) {
        financeHistoryRepository.save(FinanceHistory.record(
                ownerId,
                actorId,
                actionType,
                referenceType,
                referenceId,
                reason,
                oldValue,
                newValue
        ));
    }
}
