package com.lifebalance.finance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.domain.FinanceMonthlyJarSettlement;
import com.lifebalance.finance.domain.FinancialTransaction;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceMonthlyJarSettlementRepository;
import com.lifebalance.finance.repository.FinancialTransactionRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinanceMonthEndRolloverServiceTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MAIN_POOL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID JAR_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TRANSACTION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final OffsetDateTime AUGUST_CREATED_AT =
            OffsetDateTime.parse("2026-08-05T08:00:00+07:00");
    private static final OffsetDateTime SEPTEMBER_NOW =
            OffsetDateTime.parse("2026-09-01T00:10:00+07:00");

    @Mock
    private FinanceAccountRepository accountRepository;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private FinanceMonthlyJarSettlementRepository settlementRepository;

    @Mock
    private FinanceHistoryRecorder historyRecorder;

    @Mock
    private FinanceMonthEndJarSettlementWorker settlementWorker;

    @Test
    void returnsPositiveJarBalanceToMonthlyMainPoolAndRecordsVariance() {
        FinanceAccount mainPool = account(MAIN_POOL_ID, FinanceAccountType.MAIN_POOL, "5000.0000");
        FinanceAccount jar = account(JAR_ID, FinanceAccountType.JAR, "1000.0000");
        jar.debit(new BigDecimal("600.0000"));
        stubExpiredJar(jar);
        stubMainPool(mainPool);
        when(transactionRepository.sumPostedExpenseBySourceAccount(
                OWNER_ID,
                JAR_ID,
                "VND",
                OffsetDateTime.parse("2026-08-01T00:00:00+07:00"),
                OffsetDateTime.parse("2026-09-01T00:00:00+07:00")
        )).thenReturn(new BigDecimal("600.0000"));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setField(transaction, "id", TRANSACTION_ID);
            return transaction;
        });

        int settled = service().settleExpiredJars(SEPTEMBER_NOW);

        assertThat(settled).isEqualTo(1);
        assertThat(jar.getCurrentBalance()).isEqualByComparingTo("0.0000");
        assertThat(mainPool.getCurrentBalance()).isEqualByComparingTo("5400.0000");

        ArgumentCaptor<FinancialTransaction> transactionCaptor =
                ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        FinancialTransaction transaction = transactionCaptor.getValue();
        assertThat(transaction.isSystemGenerated()).isTrue();
        assertThat(transaction.getSourceAccount().getId()).isEqualTo(JAR_ID);
        assertThat(transaction.getDestinationAccount().getId()).isEqualTo(MAIN_POOL_ID);
        assertThat(transaction.getAmount()).isEqualByComparingTo("400.0000");

        ArgumentCaptor<FinanceMonthlyJarSettlement> settlementCaptor =
                ArgumentCaptor.forClass(FinanceMonthlyJarSettlement.class);
        verify(settlementRepository).save(settlementCaptor.capture());
        FinanceMonthlyJarSettlement settlement = settlementCaptor.getValue();
        assertThat(settlement.getAllocatedAmount()).isEqualByComparingTo("1000.0000");
        assertThat(settlement.getActualExpenseAmount()).isEqualByComparingTo("600.0000");
        assertThat(settlement.getClosingBalance()).isEqualByComparingTo("400.0000");
        assertThat(settlement.getVarianceAmount()).isEqualByComparingTo("-400.0000");
        assertThat(settlement.getSettlementTransaction()).isSameAs(transaction);
    }

    @Test
    void doesNotSettleSameJarAndMonthTwice() {
        FinanceAccount jar = account(JAR_ID, FinanceAccountType.JAR, "1000.0000");
        stubExpiredJar(jar);
        when(settlementRepository.existsByJarAccountIdAndPeriodStart(
                JAR_ID,
                LocalDate.of(2026, 8, 1)
        )).thenReturn(true);

        int settled = service().settleExpiredJars(SEPTEMBER_NOW);

        assertThat(settled).isZero();
        verify(transactionRepository, never()).save(any());
        verify(settlementRepository, never()).save(any());
    }

    @Test
    void coversNegativeJarBalanceFromMainPoolAndRecordsOverrun() {
        FinanceAccount mainPool = account(MAIN_POOL_ID, FinanceAccountType.MAIN_POOL, "5000.0000");
        FinanceAccount jar = account(JAR_ID, FinanceAccountType.JAR, "1000.0000");
        // Legacy data can contain an overdrawn jar. New expenses/transfers are
        // blocked by FinanceAccount.debit(), but month-end rollover still
        // settles this historical state and records the deficit.
        setField(jar, "currentBalance", new BigDecimal("-200.0000"));
        stubExpiredJar(jar);
        stubMainPool(mainPool);
        when(transactionRepository.sumPostedExpenseBySourceAccount(
                OWNER_ID,
                JAR_ID,
                "VND",
                OffsetDateTime.parse("2026-08-01T00:00:00+07:00"),
                OffsetDateTime.parse("2026-09-01T00:00:00+07:00")
        )).thenReturn(new BigDecimal("1200.0000"));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            setField(transaction, "id", TRANSACTION_ID);
            return transaction;
        });

        int settled = service().settleExpiredJars(SEPTEMBER_NOW);

        assertThat(settled).isEqualTo(1);
        assertThat(jar.getCurrentBalance()).isEqualByComparingTo("0.0000");
        assertThat(mainPool.getCurrentBalance()).isEqualByComparingTo("4800.0000");

        ArgumentCaptor<FinanceMonthlyJarSettlement> settlementCaptor =
                ArgumentCaptor.forClass(FinanceMonthlyJarSettlement.class);
        verify(settlementRepository).save(settlementCaptor.capture());
        assertThat(settlementCaptor.getValue().getClosingBalance()).isEqualByComparingTo("-200.0000");
        assertThat(settlementCaptor.getValue().getTransferredAmount()).isEqualByComparingTo("200.0000");
        assertThat(settlementCaptor.getValue().getVarianceAmount()).isEqualByComparingTo("200.0000");
    }

    @Test
    void continuesWithRemainingJarsWhenOneSettlementFails() {
        FinanceAccount firstJar = account(JAR_ID, FinanceAccountType.JAR, "1000.0000");
        UUID secondJarId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        FinanceAccount secondJar = account(secondJarId, FinanceAccountType.JAR, "500.0000");
        OffsetDateTime currentMonthStart = OffsetDateTime.parse("2026-09-01T00:00:00+07:00");
        when(accountRepository.findExpiredActiveJars(currentMonthStart))
                .thenReturn(List.of(firstJar, secondJar));
        when(settlementWorker.settleJar(
                JAR_ID, OWNER_ID, currentMonthStart, SEPTEMBER_NOW))
                .thenThrow(new IllegalStateException("legacy jar cannot be settled"));
        when(settlementWorker.settleJar(
                secondJarId, OWNER_ID, currentMonthStart, SEPTEMBER_NOW))
                .thenReturn(true);

        int settled = new FinanceMonthEndRolloverService(
                accountRepository,
                settlementWorker
        ).settleExpiredJars(SEPTEMBER_NOW);

        assertThat(settled).isEqualTo(1);
        verify(settlementWorker).settleJar(
                secondJarId, OWNER_ID, currentMonthStart, SEPTEMBER_NOW);
    }

    private void stubExpiredJar(FinanceAccount jar) {
        when(accountRepository.findExpiredActiveJars(
                OffsetDateTime.parse("2026-09-01T00:00:00+07:00")
        )).thenReturn(List.of(jar));
        when(accountRepository.findByIdAndOwnerIdForUpdate(JAR_ID, OWNER_ID))
                .thenReturn(Optional.of(jar));
    }

    private void stubMainPool(FinanceAccount mainPool) {
        when(accountRepository
                .findFirstByOwnerIdAndAccountTypeAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        OWNER_ID,
                        FinanceAccountType.MAIN_POOL,
                        FinanceAccountStatus.ACTIVE,
                        OffsetDateTime.parse("2026-08-01T00:00:00+07:00"),
                        OffsetDateTime.parse("2026-09-01T00:00:00+07:00")
                ))
                .thenReturn(Optional.of(mainPool));
    }

    private FinanceMonthEndRolloverService service() {
        FinanceMonthEndJarSettlementWorker worker = new FinanceMonthEndJarSettlementWorker(
                accountRepository,
                transactionRepository,
                settlementRepository,
                historyRecorder
        );
        return new FinanceMonthEndRolloverService(
                accountRepository,
                worker
        );
    }

    private static FinanceAccount account(UUID id, FinanceAccountType type, String openingBalance) {
        FinanceAccount account = FinanceAccount.create(
                OWNER_ID,
                OWNER_ID,
                type == FinanceAccountType.MAIN_POOL ? "Ví tổng" : "Hũ sinh hoạt",
                type,
                "VND",
                new BigDecimal(openingBalance)
        );
        setField(account, "id", id);
        setField(account, "createdAt", AUGUST_CREATED_AT);
        return account;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
