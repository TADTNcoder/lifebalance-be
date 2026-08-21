package com.lifebalance.finance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.common.error.AppException;
import com.lifebalance.finance.domain.FinanceBudget;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.dto.BudgetStatusResponse;
import com.lifebalance.finance.dto.CreateBudgetRequest;
import com.lifebalance.finance.dto.BudgetResponse;
import com.lifebalance.finance.error.FinanceErrorCode;
import com.lifebalance.finance.repository.FinanceBudgetRepository;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FinanceBudgetServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID BUDGET_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate PERIOD_START = LocalDate.of(2026, 8, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2026, 8, 31);

    @Mock
    private FinanceBudgetRepository budgetRepository;

    @Mock
    private FinanceCategoryRepository categoryRepository;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private FinanceHistoryRecorder historyRecorder;

    @Test
    void createBudgetStoresActiveBudgetAndRecordsHistory() {
        FinanceCategory category = category(CATEGORY_ID, FinanceCategoryType.EXPENSE);
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.existsOverlappingActiveBudget(
                OWNER_ID,
                CATEGORY_ID,
                "USD",
                PERIOD_START,
                PERIOD_END,
                null
        )).thenReturn(false);
        when(budgetRepository.save(any(FinanceBudget.class))).thenAnswer(invocation -> {
            FinanceBudget budget = invocation.getArgument(0);
            setId(budget, BUDGET_ID);
            return budget;
        });

        BudgetResponse response = createService().create(OWNER_ID, createBudgetRequest("Food budget"));

        assertThat(response.id()).isEqualTo(BUDGET_ID);
        assertThat(response.ownerId()).isEqualTo(OWNER_ID);
        assertThat(response.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(response.amountLimit()).isEqualByComparingTo("1000.0000");
        assertThat(response.alertThresholdPercent()).isEqualByComparingTo("80.00");
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(FinanceHistoryActionType.BUDGET_CREATED),
                eq(FinanceReferenceType.FINANCE_BUDGET),
                eq(BUDGET_ID),
                eq("Sprint 5 budget"),
                eq(null),
                org.mockito.ArgumentMatchers.contains("amountLimit=1000.0000")
        );
    }

    @Test
    void createRejectsOverlappingBudgetWithoutSavingHistory() {
        FinanceCategory category = category(CATEGORY_ID, FinanceCategoryType.EXPENSE);
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.existsOverlappingActiveBudget(
                OWNER_ID,
                CATEGORY_ID,
                "USD",
                PERIOD_START,
                PERIOD_END,
                null
        )).thenReturn(true);

        assertThatThrownBy(() -> createService().create(OWNER_ID, createBudgetRequest("Food budget")))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_BUDGET_ALREADY_EXISTS);

        verify(budgetRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getBudgetStatusCalculatesSpentRemainingUsageAndThreshold() {
        FinanceBudget budget = FinanceBudget.create(
                OWNER_ID,
                OWNER_ID,
                category(CATEGORY_ID, FinanceCategoryType.EXPENSE),
                "Food budget",
                PERIOD_START,
                PERIOD_END,
                amount("1000.0000"),
                "USD",
                amount("80.00")
        );
        setId(budget, BUDGET_ID);
        when(budgetRepository.findByIdAndOwnerId(BUDGET_ID, OWNER_ID)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumPostedAmount(
                eq(OWNER_ID),
                eq(FinanceTransactionType.EXPENSE),
                eq("USD"),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                eq(CATEGORY_ID)
        )).thenReturn(amount("850.0000"));

        BudgetStatusResponse response = createService().getBudgetStatus(OWNER_ID, BUDGET_ID);

        assertThat(response.budget().id()).isEqualTo(BUDGET_ID);
        assertThat(response.spentAmount()).isEqualByComparingTo("850.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("150.0000");
        assertThat(response.usagePercent()).isEqualByComparingTo("85.00");
        assertThat(response.thresholdReached()).isTrue();
        assertThat(response.historyReferenceId()).isEqualTo(BUDGET_ID);
    }

    @Test
    void createRejectsIncomeCategoryForExpenseBudget() {
        FinanceCategory category = category(CATEGORY_ID, FinanceCategoryType.INCOME);
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> createService().create(OWNER_ID, createBudgetRequest("Income budget")))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_CATEGORY_TYPE_MISMATCH);

        verify(budgetRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    private FinanceBudgetServiceImpl createService() {
        return new FinanceBudgetServiceImpl(
                budgetRepository,
                categoryRepository,
                transactionRepository,
                historyRecorder
        );
    }

    private static CreateBudgetRequest createBudgetRequest(String name) {
        return new CreateBudgetRequest(
                CATEGORY_ID,
                name,
                PERIOD_START,
                PERIOD_END,
                amount("1000.0000"),
                "USD",
                amount("80.00"),
                "Sprint 5 budget"
        );
    }

    private static FinanceCategory category(UUID id, FinanceCategoryType categoryType) {
        FinanceCategory category = FinanceCategory.create(
                OWNER_ID,
                OWNER_ID,
                categoryType == FinanceCategoryType.EXPENSE ? "Food" : "Salary",
                categoryType,
                null,
                null
        );
        setId(category, id);
        return category;
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static void setId(Object target, UUID id) {
        ReflectionTestUtils.setField(target, "id", id);
    }
}
