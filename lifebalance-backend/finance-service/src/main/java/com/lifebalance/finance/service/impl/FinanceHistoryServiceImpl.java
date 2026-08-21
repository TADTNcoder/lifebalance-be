package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.dto.FinanceHistoryResponse;
import com.lifebalance.finance.repository.FinanceHistoryRepository;
import com.lifebalance.finance.service.FinanceHistoryService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceHistoryServiceImpl implements FinanceHistoryService {

    private final FinanceHistoryRepository financeHistoryRepository;

    public FinanceHistoryServiceImpl(FinanceHistoryRepository financeHistoryRepository) {
        this.financeHistoryRepository = financeHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinanceHistoryResponse> getHistory(
            UUID ownerId,
            FinanceReferenceType referenceType,
            UUID referenceId,
            Pageable pageable
    ) {
        if (referenceType != null && referenceId != null) {
            return financeHistoryRepository
                    .findByOwnerIdAndReferenceTypeAndReferenceIdOrderByOccurredAtDesc(
                            ownerId,
                            referenceType,
                            referenceId,
                            pageable)
                    .map(FinanceMapper::toHistoryResponse);
        }

        return financeHistoryRepository.findByOwnerIdOrderByOccurredAtDesc(ownerId, pageable)
                .map(FinanceMapper::toHistoryResponse);
    }
}
