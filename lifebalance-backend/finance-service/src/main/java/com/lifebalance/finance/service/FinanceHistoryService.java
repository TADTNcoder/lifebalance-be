package com.lifebalance.finance.service;

import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.dto.FinanceHistoryResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FinanceHistoryService {

    Page<FinanceHistoryResponse> getHistory(UUID ownerId, FinanceReferenceType referenceType, UUID referenceId, Pageable pageable);
}
