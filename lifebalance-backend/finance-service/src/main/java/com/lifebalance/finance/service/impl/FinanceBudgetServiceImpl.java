package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.BudgetStatus;
import com.lifebalance.finance.domain.FinanceBudget;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.dto.BudgetResponse;
import com.lifebalance.finance.dto.BudgetStatusResponse;
import com.lifebalance.finance.dto.CreateBudgetRequest;
import com.lifebalance.finance.dto.UpdateBudgetRequest;
import com.lifebalance.finance.error.FinanceExceptions;
import com.lifebalance.finance.repository.FinanceBudgetRepository;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import com.lifebalance.finance.service.FinanceBudgetService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceBudgetServiceImpl implements FinanceBudgetService {

    private static final BigDecimal DEFAULT_ALERT_THRESHOLD = new BigDecimal("80.00");

    private final FinanceBudgetRepository budgetRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final FinanceHistoryRecorder historyRecorder;

    public FinanceBudgetServiceImpl(
            FinanceBudgetRepository budgetRepository,
            FinanceCategoryRepository categoryRepository,
            FinancialTransactionRepository transactionRepository,
            FinanceHistoryRecorder historyRecorder
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.historyRecorder = historyRecorder;
    }

    @Override
    @Transactional
    public BudgetResponse create(UUID ownerId, CreateBudgetRequest request) {
        FinanceSupport.ensurePeriod(request.periodStart(), request.periodEnd());
        String currencyCode = FinanceSupport.normalizeCurrency(request.currencyCode());
        BigDecimal amountLimit = FinanceSupport.normalizeAmount(request.amountLimit());
        BigDecimal threshold = request.alertThresholdPercent() == null
                ? DEFAULT_ALERT_THRESHOLD
                : request.alertThresholdPercent().setScale(2, RoundingMode.UNNECESSARY);
        FinanceCategory category = resolveExpenseCategory(ownerId, request.categoryId());

        ensureNoOverlap(ownerId, request.categoryId(), currencyCode, request.periodStart(), request.periodEnd(), null);

        FinanceBudget budget = FinanceBudget.create(
                ownerId,
                ownerId,
                category,
                request.name().trim(),
                request.periodStart(),
                request.periodEnd(),
                amountLimit,
                currencyCode,
                threshold
        );
        budget = budgetRepository.save(budget);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.BUDGET_CREATED,
                FinanceReferenceType.FINANCE_BUDGET,
                budget.getId(),
                request.reason(),
                null,
                snapshot(budget)
        );

        return FinanceMapper.toBudgetResponse(budget);
    }

    @Override
    @Transactional
    public BudgetResponse update(UUID ownerId, UUID budgetId, UpdateBudgetRequest request) {
        FinanceBudget budget = getOwnedBudget(ownerId, budgetId);
        FinanceSupport.ensurePeriod(request.periodStart(), request.periodEnd());
        String currencyCode = FinanceSupport.normalizeCurrency(request.currencyCode());
        BigDecimal amountLimit = FinanceSupport.normalizeAmount(request.amountLimit());
        BigDecimal threshold = request.alertThresholdPercent().setScale(2, RoundingMode.UNNECESSARY);
        FinanceCategory category = resolveExpenseCategory(ownerId, request.categoryId());

        ensureNoOverlap(ownerId, request.categoryId(), currencyCode, request.periodStart(), request.periodEnd(), budgetId);

        String oldValue = snapshot(budget);
        budget.updateDetails(
                ownerId,
                category,
                request.name().trim(),
                request.periodStart(),
                request.periodEnd(),
                amountLimit,
                currencyCode,
                threshold
        );
        budget = budgetRepository.save(budget);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.BUDGET_UPDATED,
                FinanceReferenceType.FINANCE_BUDGET,
                budget.getId(),
                request.reason(),
                oldValue,
                snapshot(budget)
        );

        return FinanceMapper.toBudgetResponse(budget);
    }

    @Override
    @Transactional
    public BudgetResponse archive(UUID ownerId, UUID budgetId, String reason) {
        FinanceBudget budget = getOwnedBudget(ownerId, budgetId);
        String oldValue = snapshot(budget);
        budget.archive(ownerId);
        budget = budgetRepository.save(budget);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.BUDGET_ARCHIVED,
                FinanceReferenceType.FINANCE_BUDGET,
                budget.getId(),
                reason,
                oldValue,
                snapshot(budget)
        );

        return FinanceMapper.toBudgetResponse(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getById(UUID ownerId, UUID budgetId) {
        return FinanceMapper.toBudgetResponse(getOwnedBudget(ownerId, budgetId));
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetStatusResponse getBudgetStatus(UUID ownerId, UUID budgetId) {
        FinanceBudget budget = getOwnedBudget(ownerId, budgetId);
        UUID categoryId = budget.getCategory() == null ? null : budget.getCategory().getId();
        BigDecimal spentAmount = transactionRepository.sumPostedAmount(
                ownerId,
                FinanceTransactionType.EXPENSE,
                budget.getCurrencyCode(),
                startOfDay(budget.getPeriodStart()),
                endOfDay(budget.getPeriodEnd()),
                categoryId
        );
        BigDecimal remainingAmount = budget.getAmountLimit().subtract(spentAmount);
        BigDecimal usagePercent = budget.getAmountLimit().signum() == 0
                ? BigDecimal.ZERO.setScale(2)
                : spentAmount
                .multiply(FinanceSupport.ONE_HUNDRED)
                .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP);

        return new BudgetStatusResponse(
                FinanceMapper.toBudgetResponse(budget),
                spentAmount,
                remainingAmount,
                usagePercent,
                usagePercent.compareTo(budget.getAlertThresholdPercent()) >= 0,
                budget.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetResponse> getBudgets(
            UUID ownerId,
            BudgetStatus status,
            String currencyCode,
            UUID categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        String normalizedCurrency = currencyCode == null || currencyCode.isBlank()
                ? null
                : FinanceSupport.normalizeCurrency(currencyCode);

        return budgetRepository.search(ownerId, status, normalizedCurrency, categoryId, fromDate, toDate, pageable)
                .map(FinanceMapper::toBudgetResponse);
    }

    private FinanceBudget getOwnedBudget(UUID ownerId, UUID budgetId) {
        return budgetRepository.findByIdAndOwnerId(budgetId, ownerId)
                .orElseThrow(() -> FinanceExceptions.budgetNotFound(budgetId));
    }

    private FinanceCategory resolveExpenseCategory(UUID ownerId, UUID categoryId) {
        if (categoryId == null) {
            return null;
        }

        FinanceCategory category = categoryRepository.findByIdAndOwnerId(categoryId, ownerId)
                .orElseThrow(() -> FinanceExceptions.categoryNotFound(categoryId));
        if (!category.isActive()) {
            throw FinanceExceptions.categoryNotFound(categoryId);
        }

        FinanceSupport.ensureCategoryType(category, FinanceCategoryType.EXPENSE);
        return category;
    }

    private void ensureNoOverlap(
            UUID ownerId,
            UUID categoryId,
            String currencyCode,
            LocalDate periodStart,
            LocalDate periodEnd,
            UUID excludeBudgetId
    ) {
        if (budgetRepository.existsOverlappingActiveBudget(
                ownerId,
                categoryId,
                currencyCode,
                periodStart,
                periodEnd,
                excludeBudgetId
        )) {
            throw FinanceExceptions.budgetAlreadyExists(categoryId, periodStart, periodEnd);
        }
    }

    private static OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private static OffsetDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
    }

    private static String snapshot(FinanceBudget budget) {
        return "name=" + budget.getName()
                + ";categoryId=" + (budget.getCategory() == null ? null : budget.getCategory().getId())
                + ";periodStart=" + budget.getPeriodStart()
                + ";periodEnd=" + budget.getPeriodEnd()
                + ";amountLimit=" + budget.getAmountLimit()
                + ";currency=" + budget.getCurrencyCode()
                + ";threshold=" + budget.getAlertThresholdPercent()
                + ";status=" + budget.getStatus();
    }
}
