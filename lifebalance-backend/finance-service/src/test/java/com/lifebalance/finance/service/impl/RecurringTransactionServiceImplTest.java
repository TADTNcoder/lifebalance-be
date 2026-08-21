package com.lifebalance.finance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.common.error.AppException;
import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.domain.RecurrenceFrequency;
import com.lifebalance.finance.domain.RecurringTransactionRule;
import com.lifebalance.finance.domain.RecurringTransactionStatus;
import com.lifebalance.finance.dto.RecurringTransactionResponse;
import com.lifebalance.finance.error.FinanceErrorCode;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.repository.RecurringTransactionRuleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID RULE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ACCOUNT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private RecurringTransactionRuleRepository recurringRuleRepository;

    @Mock
    private FinanceAccountRepository accountRepository;

    @Mock
    private FinanceCategoryRepository categoryRepository;

    @Mock
    private FinanceHistoryRecorder historyRecorder;

    @Test
    void pauseActiveRuleChangesStateAndRecordsHistory() {
        RecurringTransactionRule rule = recurringRule(RecurringTransactionStatus.ACTIVE);
        when(recurringRuleRepository.findByIdAndOwnerId(RULE_ID, OWNER_ID)).thenReturn(Optional.of(rule));
        when(recurringRuleRepository.save(rule)).thenReturn(rule);

        RecurringTransactionResponse response = createService().pause(OWNER_ID, RULE_ID, "Vacation");

        assertThat(response.status()).isEqualTo(RecurringTransactionStatus.PAUSED);
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(FinanceHistoryActionType.RECURRING_TRANSACTION_PAUSED),
                eq(FinanceReferenceType.RECURRING_TRANSACTION_RULE),
                eq(RULE_ID),
                eq("Vacation"),
                org.mockito.ArgumentMatchers.contains("status=ACTIVE"),
                org.mockito.ArgumentMatchers.contains("status=PAUSED")
        );
    }

    @Test
    void resumePausedRuleChangesStateAndRecordsHistory() {
        RecurringTransactionRule rule = recurringRule(RecurringTransactionStatus.PAUSED);
        when(recurringRuleRepository.findByIdAndOwnerId(RULE_ID, OWNER_ID)).thenReturn(Optional.of(rule));
        when(recurringRuleRepository.save(rule)).thenReturn(rule);

        RecurringTransactionResponse response = createService().resume(OWNER_ID, RULE_ID, "Back on track");

        assertThat(response.status()).isEqualTo(RecurringTransactionStatus.ACTIVE);
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(FinanceHistoryActionType.RECURRING_TRANSACTION_RESUMED),
                eq(FinanceReferenceType.RECURRING_TRANSACTION_RULE),
                eq(RULE_ID),
                eq("Back on track"),
                org.mockito.ArgumentMatchers.contains("status=PAUSED"),
                org.mockito.ArgumentMatchers.contains("status=ACTIVE")
        );
    }

    @Test
    void pauseEndedRuleIsRejectedWithoutSavingHistory() {
        RecurringTransactionRule rule = recurringRule(RecurringTransactionStatus.ENDED);
        when(recurringRuleRepository.findByIdAndOwnerId(RULE_ID, OWNER_ID)).thenReturn(Optional.of(rule));

        assertThatThrownBy(() -> createService().pause(OWNER_ID, RULE_ID, "Already done"))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_RECURRING_RULE_INVALID);

        verify(recurringRuleRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void resumeActiveRuleIsRejectedWithoutSavingHistory() {
        RecurringTransactionRule rule = recurringRule(RecurringTransactionStatus.ACTIVE);
        when(recurringRuleRepository.findByIdAndOwnerId(RULE_ID, OWNER_ID)).thenReturn(Optional.of(rule));

        assertThatThrownBy(() -> createService().resume(OWNER_ID, RULE_ID, "Not paused"))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_RECURRING_RULE_INVALID);

        verify(recurringRuleRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    private RecurringTransactionServiceImpl createService() {
        return new RecurringTransactionServiceImpl(
                recurringRuleRepository,
                accountRepository,
                categoryRepository,
                historyRecorder
        );
    }

    private static RecurringTransactionRule recurringRule(RecurringTransactionStatus status) {
        FinanceAccount destinationAccount = FinanceAccount.create(
                OWNER_ID,
                OWNER_ID,
                "Cash",
                FinanceAccountType.CASH,
                "USD",
                new BigDecimal("100.0000")
        );
        ReflectionTestUtils.setField(destinationAccount, "id", ACCOUNT_ID);

        RecurringTransactionRule rule = RecurringTransactionRule.create(
                OWNER_ID,
                OWNER_ID,
                FinanceTransactionType.INCOME,
                null,
                destinationAccount,
                null,
                new BigDecimal("25.0000"),
                "USD",
                RecurrenceFrequency.MONTHLY,
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 1),
                null,
                "Monthly income"
        );
        ReflectionTestUtils.setField(rule, "id", RULE_ID);

        if (status == RecurringTransactionStatus.PAUSED) {
            rule.pause(OWNER_ID);
        }
        if (status == RecurringTransactionStatus.ENDED) {
            rule.end(OWNER_ID);
        }

        return rule;
    }
}
