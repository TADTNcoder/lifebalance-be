package com.lifebalance.finance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.common.error.AppException;
import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceIncomeSourceType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionStatus;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.FinancialTransaction;
import com.lifebalance.finance.dto.CreateTransactionRequest;
import com.lifebalance.finance.dto.TransactionResponse;
import com.lifebalance.finance.dto.VoidTransactionRequest;
import com.lifebalance.finance.error.FinanceErrorCode;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FinancialTransactionServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOURCE_ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DESTINATION_ACCOUNT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID CATEGORY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID TRANSACTION_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID TASK_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final OffsetDateTime TRANSACTION_DATE = OffsetDateTime.parse("2026-08-21T08:30:00Z");

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private FinanceAccountRepository accountRepository;

    @Mock
    private FinanceCategoryRepository categoryRepository;

    @Mock
    private FinanceHistoryRecorder historyRecorder;

    @Test
    void createExpenseDebitsSourceAccountAndRecordsHistory() {
        FinanceAccount sourceAccount = account(SOURCE_ACCOUNT_ID, "Daily wallet", "USD", "500.0000");
        FinanceCategory category = category(CATEGORY_ID, "Food", FinanceCategoryType.EXPENSE);
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setId(transaction, TRANSACTION_ID);
            return transaction;
        });

        TransactionResponse response = createService().create(OWNER_ID, new CreateTransactionRequest(
                FinanceTransactionType.EXPENSE,
                SOURCE_ACCOUNT_ID,
                null,
                CATEGORY_ID,
                amount("125.5000"),
                "USD",
                TRANSACTION_DATE,
                "Bữa trưa nhóm",
                "Team lunch",
                null,
                null,
                null,
                "Lunch budget"
        ));

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("374.5000");
        assertThat(response.id()).isEqualTo(TRANSACTION_ID);
        assertThat(response.transactionType()).isEqualTo(FinanceTransactionType.EXPENSE);
        assertThat(response.status()).isEqualTo(FinanceTransactionStatus.POSTED);
        assertThat(response.sourceAccountId()).isEqualTo(SOURCE_ACCOUNT_ID);
        assertThat(response.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(response.transactionName()).isEqualTo("Bữa trưa nhóm");
        assertThat(response.amount()).isEqualByComparingTo("125.5000");

        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(FinanceHistoryActionType.TRANSACTION_CREATED),
                eq(FinanceReferenceType.FINANCIAL_TRANSACTION),
                eq(TRANSACTION_ID),
                eq("Lunch budget"),
                eq(null),
                org.mockito.ArgumentMatchers.contains("amount=125.5000")
        );
    }

    @Test
    void createRejectsCurrencyMismatchWithoutChangingBalanceOrHistory() {
        FinanceAccount sourceAccount = account(SOURCE_ACCOUNT_ID, "Daily wallet", "USD", "500.0000");
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> createService().create(OWNER_ID, new CreateTransactionRequest(
                FinanceTransactionType.EXPENSE,
                SOURCE_ACCOUNT_ID,
                null,
                null,
                amount("25.0000"),
                "EUR",
                TRANSACTION_DATE,
                "Foreign expense",
                null,
                null,
                null,
                "Currency mismatch"
        )))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_CURRENCY_MISMATCH);

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.0000");
        verify(transactionRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void voidPostedExpenseCreditsSourceAccountAndRecordsHistory() {
        FinanceAccount sourceAccount = account(SOURCE_ACCOUNT_ID, "Daily wallet", "USD", "300.0000");
        FinanceCategory category = category(CATEGORY_ID, "Food", FinanceCategoryType.EXPENSE);
        FinancialTransaction transaction = FinancialTransaction.post(
                OWNER_ID,
                OWNER_ID,
                FinanceTransactionType.EXPENSE,
                sourceAccount,
                null,
                category,
                amount("50.0000"),
                "USD",
                TRANSACTION_DATE,
                "Dinner",
                null,
                null,
                null
        );
        setId(transaction, TRANSACTION_ID);

        when(transactionRepository.findDetailedByIdAndOwnerId(TRANSACTION_ID, OWNER_ID))
                .thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        TransactionResponse response = createService().voidTransaction(
                OWNER_ID,
                TRANSACTION_ID,
                new VoidTransactionRequest("Duplicate entry")
        );

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("350.0000");
        assertThat(response.status()).isEqualTo(FinanceTransactionStatus.VOIDED);
        assertThat(response.voidReason()).isEqualTo("Duplicate entry");
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(FinanceHistoryActionType.TRANSACTION_VOIDED),
                eq(FinanceReferenceType.FINANCIAL_TRANSACTION),
                eq(TRANSACTION_ID),
                eq("Duplicate entry"),
                org.mockito.ArgumentMatchers.contains("status=POSTED"),
                org.mockito.ArgumentMatchers.contains("status=VOIDED")
        );
    }

    @Test
    void createTransferMovesBalanceBetweenAccounts() {
        FinanceAccount sourceAccount = account(
                SOURCE_ACCOUNT_ID,
                "Main pool",
                FinanceAccountType.MAIN_POOL,
                "USD",
                "1000.0000"
        );
        FinanceAccount destinationAccount = account(
                DESTINATION_ACCOUNT_ID,
                "Savings jar",
                FinanceAccountType.JAR,
                "USD",
                "200.0000"
        );
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setId(transaction, TRANSACTION_ID);
            return transaction;
        });

        TransactionResponse response = createService().create(OWNER_ID, new CreateTransactionRequest(
                FinanceTransactionType.TRANSFER,
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                null,
                amount("300.0000"),
                "USD",
                TRANSACTION_DATE,
                "Move to savings",
                null,
                null,
                null,
                "Monthly saving"
        ));

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("700.0000");
        assertThat(sourceAccount.getOpeningBalance()).isEqualByComparingTo("1000.0000");
        assertThat(destinationAccount.getCurrentBalance()).isEqualByComparingTo("500.0000");
        assertThat(destinationAccount.getOpeningBalance()).isEqualByComparingTo("500.0000");
        assertThat(response.sourceAccountId()).isEqualTo(SOURCE_ACCOUNT_ID);
        assertThat(response.destinationAccountId()).isEqualTo(DESTINATION_ACCOUNT_ID);
    }

    @Test
    void createMonthlySalaryCreditsDestinationAndPersistsSalaryBreakdown() {
        FinanceAccount destinationAccount = account(DESTINATION_ACCOUNT_ID, "Salary wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Salary", FinanceCategoryType.INCOME);
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setId(transaction, TRANSACTION_ID);
            return transaction;
        });

        TransactionResponse response = createService().create(OWNER_ID, monthlySalaryRequest("1050.0000"));

        assertThat(destinationAccount.getCurrentBalance()).isEqualByComparingTo("1150.0000");
        assertThat(response.incomeSourceType()).isEqualTo(FinanceIncomeSourceType.MONTHLY_SALARY);
        assertThat(response.salaryPeriod()).isEqualTo("2026-08");
        assertThat(response.baseSalary()).isEqualByComparingTo("1000.0000");
        assertThat(response.bonusAmount()).isEqualByComparingTo("100.0000");
        assertThat(response.deductionAmount()).isEqualByComparingTo("50.0000");
        assertThat(response.taskId()).isEqualTo(TASK_ID);
    }

    @Test
    void createMonthlySalaryRejectsAmountThatDoesNotMatchBreakdown() {
        FinanceAccount destinationAccount = account(DESTINATION_ACCOUNT_ID, "Salary wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Salary", FinanceCategoryType.INCOME);
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));

        assertThatThrownBy(() -> createService().create(OWNER_ID, monthlySalaryRequest("1000.0000")))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_TRANSACTION_INVALID);

        assertThat(destinationAccount.getCurrentBalance()).isEqualByComparingTo("100.0000");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createMonthlySalaryRejectsDuplicateTaskAndPeriod() {
        FinanceAccount destinationAccount = account(DESTINATION_ACCOUNT_ID, "Salary wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Salary", FinanceCategoryType.INCOME);
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));
        when(transactionRepository.existsPostedMonthlySalary(OWNER_ID, TASK_ID, "2026-08", null))
                .thenReturn(true);

        assertThatThrownBy(() -> createService().create(OWNER_ID, monthlySalaryRequest("1050.0000")))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_MONTHLY_SALARY_ALREADY_EXISTS);

        assertThat(destinationAccount.getCurrentBalance()).isEqualByComparingTo("100.0000");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createMonthlySalaryTranslatesConcurrentUniqueConstraintFailure() {
        FinanceAccount destinationAccount = account(DESTINATION_ACCOUNT_ID, "Salary wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Salary", FinanceCategoryType.INCOME);
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setId(transaction, TRANSACTION_ID);
            return transaction;
        });
        doThrow(new DataIntegrityViolationException("duplicate posted monthly salary"))
                .when(transactionRepository).flush();

        assertThatThrownBy(() -> createService().create(OWNER_ID, monthlySalaryRequest("1050.0000")))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_MONTHLY_SALARY_ALREADY_EXISTS);
    }

    @Test
    void createOneOffTaskIncomeRejectsDuplicateTask() {
        FinanceAccount destinationAccount = account(DESTINATION_ACCOUNT_ID, "Income wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Task income", FinanceCategoryType.INCOME);
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));
        when(transactionRepository.existsPostedOneOffTaskIncome(OWNER_ID, TASK_ID, null))
                .thenReturn(true);

        assertThatThrownBy(() -> createService().create(OWNER_ID, oneOffTaskIncomeRequest()))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_ONE_OFF_TASK_INCOME_ALREADY_EXISTS);

        assertThat(destinationAccount.getCurrentBalance()).isEqualByComparingTo("100.0000");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createOneOffTaskIncomeTranslatesConcurrentUniqueConstraintFailure() {
        FinanceAccount destinationAccount = account(DESTINATION_ACCOUNT_ID, "Income wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Task income", FinanceCategoryType.INCOME);
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setId(transaction, TRANSACTION_ID);
            return transaction;
        });
        doThrow(new DataIntegrityViolationException("duplicate posted one-off task income"))
                .when(transactionRepository).flush();

        assertThatThrownBy(() -> createService().create(OWNER_ID, oneOffTaskIncomeRequest()))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_ONE_OFF_TASK_INCOME_ALREADY_EXISTS);
    }

    @Test
    void createRejectsAccountOutsideItsCreationMonth() {
        FinanceAccount sourceAccount = account(SOURCE_ACCOUNT_ID, "Daily wallet", "USD", "500.0000");
        ReflectionTestUtils.setField(sourceAccount, "createdAt", TRANSACTION_DATE.minusMonths(1));
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> createService().create(OWNER_ID, new CreateTransactionRequest(
                FinanceTransactionType.EXPENSE,
                SOURCE_ACCOUNT_ID,
                null,
                null,
                amount("25.0000"),
                "USD",
                TRANSACTION_DATE,
                "Expired wallet expense",
                null,
                null,
                null,
                "Outside account month"
        )))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_ACCOUNT_INVALID);

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.0000");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void voidTransferRestoresJarOpeningAndCurrentBalances() {
        FinanceAccount sourceAccount = account(
                SOURCE_ACCOUNT_ID,
                "Main pool",
                FinanceAccountType.MAIN_POOL,
                "USD",
                "1000.0000"
        );
        FinanceAccount destinationAccount = account(
                DESTINATION_ACCOUNT_ID,
                "Savings jar",
                FinanceAccountType.JAR,
                "USD",
                "200.0000"
        );
        sourceAccount.debit(amount("300.0000"));
        destinationAccount.receiveTransfer(amount("300.0000"));

        FinancialTransaction transaction = FinancialTransaction.post(
                OWNER_ID,
                OWNER_ID,
                FinanceTransactionType.TRANSFER,
                sourceAccount,
                destinationAccount,
                null,
                amount("300.0000"),
                "USD",
                TRANSACTION_DATE,
                "Move to savings",
                null,
                null,
                null
        );
        setId(transaction, TRANSACTION_ID);

        when(transactionRepository.findDetailedByIdAndOwnerId(TRANSACTION_ID, OWNER_ID))
                .thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdAndOwnerIdForUpdate(DESTINATION_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        createService().voidTransaction(
                OWNER_ID,
                TRANSACTION_ID,
                new VoidTransactionRequest("Undo transfer")
        );

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("1000.0000");
        assertThat(sourceAccount.getOpeningBalance()).isEqualByComparingTo("1000.0000");
        assertThat(destinationAccount.getCurrentBalance()).isEqualByComparingTo("200.0000");
        assertThat(destinationAccount.getOpeningBalance()).isEqualByComparingTo("200.0000");
    }

    @Test
    void getTransactionsUsesTypedDateBoundsWhenDateFiltersAreMissing() {
        Pageable pageable = PageRequest.of(0, 20);
        when(transactionRepository.search(
                eq(OWNER_ID),
                nullable(FinanceTransactionType.class),
                nullable(FinanceTransactionStatus.class),
                nullable(UUID.class),
                nullable(UUID.class),
                nullable(UUID.class),
                nullable(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        createService().getTransactions(
                OWNER_ID,
                null,
                FinanceTransactionStatus.POSTED,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        );

        ArgumentCaptor<OffsetDateTime> fromDate = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toDate = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(transactionRepository).search(
                eq(OWNER_ID),
                nullable(FinanceTransactionType.class),
                eq(FinanceTransactionStatus.POSTED),
                nullable(UUID.class),
                nullable(UUID.class),
                nullable(UUID.class),
                nullable(UUID.class),
                fromDate.capture(),
                toDate.capture(),
                eq(pageable)
        );

        assertThat(fromDate.getValue()).isEqualTo(OffsetDateTime.parse("0001-01-01T00:00:00Z"));
        assertThat(toDate.getValue()).isEqualTo(OffsetDateTime.parse("9999-12-31T23:59:59.999999999Z"));
    }

    private FinancialTransactionServiceImpl createService() {
        return new FinancialTransactionServiceImpl(
                transactionRepository,
                accountRepository,
                categoryRepository,
                historyRecorder
        );
    }

    private static FinanceAccount account(UUID id, String name, String currencyCode, String currentBalance) {
        return account(id, name, FinanceAccountType.JAR, currencyCode, currentBalance);
    }

    private static CreateTransactionRequest monthlySalaryRequest(String netAmount) {
        return new CreateTransactionRequest(
                FinanceTransactionType.INCOME,
                null,
                DESTINATION_ACCOUNT_ID,
                CATEGORY_ID,
                amount(netAmount),
                "USD",
                TRANSACTION_DATE,
                "Salary August 2026",
                "Monthly salary",
                TASK_ID,
                null,
                null,
                FinanceIncomeSourceType.MONTHLY_SALARY,
                "2026-08",
                amount("1000.0000"),
                amount("100.0000"),
                amount("50.0000"),
                "Record received salary"
        );
    }

    private static CreateTransactionRequest oneOffTaskIncomeRequest() {
        return new CreateTransactionRequest(
                FinanceTransactionType.INCOME,
                null,
                DESTINATION_ACCOUNT_ID,
                CATEGORY_ID,
                amount("250.0000"),
                "USD",
                TRANSACTION_DATE,
                "Task income",
                "One-off task income",
                TASK_ID,
                null,
                null,
                FinanceIncomeSourceType.ONE_OFF,
                null,
                null,
                null,
                null,
                "Settle completed task"
        );
    }

    @Test
    void createExpenseRejectsAmountThatWouldMakeSourceBalanceNegative() {
        FinanceAccount sourceAccount = account(SOURCE_ACCOUNT_ID, "Daily wallet", "USD", "100.0000");
        FinanceCategory category = category(CATEGORY_ID, "Food", FinanceCategoryType.EXPENSE);
        when(accountRepository.findByIdAndOwnerIdForUpdate(SOURCE_ACCOUNT_ID, OWNER_ID))
                .thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findByIdAndOwnerId(CATEGORY_ID, OWNER_ID))
                .thenReturn(Optional.of(category));

        assertThatThrownBy(() -> createService().create(OWNER_ID, new CreateTransactionRequest(
                FinanceTransactionType.EXPENSE,
                SOURCE_ACCOUNT_ID,
                null,
                CATEGORY_ID,
                amount("125.0000"),
                "USD",
                TRANSACTION_DATE,
                "Chi vượt số dư",
                null,
                null,
                null,
                "Insufficient balance"
        )))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_INSUFFICIENT_BALANCE);

        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("100.0000");
        verify(transactionRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    private static FinanceAccount account(
            UUID id,
            String name,
            FinanceAccountType accountType,
            String currencyCode,
            String currentBalance
    ) {
        FinanceAccount account = FinanceAccount.create(
                OWNER_ID,
                OWNER_ID,
                name,
                accountType,
                currencyCode,
                amount(currentBalance)
        );
        setId(account, id);
        ReflectionTestUtils.setField(account, "createdAt", TRANSACTION_DATE.minusDays(1));
        return account;
    }

    private static FinanceCategory category(UUID id, String name, FinanceCategoryType categoryType) {
        FinanceCategory category = FinanceCategory.create(OWNER_ID, OWNER_ID, name, categoryType, null, null);
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
