package com.lifebalance.finance.service;

import com.lifebalance.finance.domain.BudgetStatus;
import com.lifebalance.finance.dto.BudgetResponse;
import com.lifebalance.finance.dto.BudgetStatusResponse;
import com.lifebalance.finance.dto.CreateBudgetRequest;
import com.lifebalance.finance.dto.UpdateBudgetRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FinanceBudgetService {

    BudgetResponse create(UUID ownerId, CreateBudgetRequest request);

    BudgetResponse update(UUID ownerId, UUID budgetId, UpdateBudgetRequest request);

    BudgetResponse archive(UUID ownerId, UUID budgetId, String reason);

    BudgetResponse getById(UUID ownerId, UUID budgetId);

    BudgetStatusResponse getBudgetStatus(UUID ownerId, UUID budgetId);

    Page<BudgetResponse> getBudgets(
            UUID ownerId,
            BudgetStatus status,
            String currencyCode,
            UUID categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable);
}
