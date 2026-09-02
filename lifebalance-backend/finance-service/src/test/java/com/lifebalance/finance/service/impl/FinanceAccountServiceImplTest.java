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
import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.domain.FinanceAccountType;
import com.lifebalance.finance.dto.CreateFinanceAccountRequest;
import com.lifebalance.finance.error.FinanceErrorCode;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FinanceAccountServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private FinanceAccountRepository accountRepository;

    @Mock
    private FinanceHistoryRecorder historyRecorder;

    @Test
    void createsSingleMainPoolWithOpeningBalance() {
        when(accountRepository.existsActiveMainPool(OWNER_ID)).thenReturn(false);
        when(accountRepository.existsNameInCreatedPeriod(
                eq(OWNER_ID),
                eq("Ví tổng"),
                eq(FinanceAccountStatus.ACTIVE),
                any(),
                any()
        )).thenReturn(false);
        when(accountRepository.save(any(FinanceAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service().create(OWNER_ID, new CreateFinanceAccountRequest(
                " Ví tổng ",
                FinanceAccountType.MAIN_POOL,
                "vnd",
                new BigDecimal("1000000")
        ));

        assertThat(response.name()).isEqualTo("Ví tổng");
        assertThat(response.accountType()).isEqualTo(FinanceAccountType.MAIN_POOL);
        assertThat(response.currentBalance()).isEqualByComparingTo("1000000.0000");
    }

    @Test
    void rejectsSecondActiveMainPool() {
        when(accountRepository.existsActiveMainPool(OWNER_ID)).thenReturn(true);

        assertFinanceAccountInvalid(() -> service().create(OWNER_ID, new CreateFinanceAccountRequest(
                "Ví tổng khác",
                FinanceAccountType.MAIN_POOL,
                "VND",
                BigDecimal.ZERO
        )));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createsZeroBalanceJarUsingLifetimeMainPoolCurrency() {
        FinanceAccount mainPool = FinanceAccount.create(
                OWNER_ID,
                OWNER_ID,
                "Ví tổng",
                FinanceAccountType.MAIN_POOL,
                "VND",
                new BigDecimal("1000000.0000")
        );
        ReflectionTestUtils.setField(
                mainPool,
                "createdAt",
                OffsetDateTime.parse("2026-01-15T00:00:00+07:00")
        );
        when(accountRepository.findFirstByOwnerIdAndAccountTypeAndStatusOrderByCreatedAtAscIdAsc(
                OWNER_ID, FinanceAccountType.MAIN_POOL, FinanceAccountStatus.ACTIVE
        )).thenReturn(Optional.of(mainPool));
        when(accountRepository.existsNameInCreatedPeriod(
                eq(OWNER_ID),
                eq("Ăn uống"),
                eq(FinanceAccountStatus.ACTIVE),
                any(),
                any()
        )).thenReturn(false);
        when(accountRepository.save(any(FinanceAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service().create(OWNER_ID, new CreateFinanceAccountRequest(
                "Ăn uống",
                FinanceAccountType.JAR,
                "VND",
                BigDecimal.ZERO
        ));

        assertThat(response.accountType()).isEqualTo(FinanceAccountType.JAR);
        assertThat(response.currentBalance()).isEqualByComparingTo("0.0000");
    }

    @Test
    void rejectsJarWithIndependentOpeningBalance() {
        FinanceAccount mainPool = FinanceAccount.create(
                OWNER_ID,
                OWNER_ID,
                "Ví tổng",
                FinanceAccountType.MAIN_POOL,
                "VND",
                BigDecimal.ZERO.setScale(4)
        );
        when(accountRepository.findFirstByOwnerIdAndAccountTypeAndStatusOrderByCreatedAtAscIdAsc(
                OWNER_ID, FinanceAccountType.MAIN_POOL, FinanceAccountStatus.ACTIVE
        )).thenReturn(Optional.of(mainPool));

        assertFinanceAccountInvalid(() -> service().create(OWNER_ID, new CreateFinanceAccountRequest(
                "Du lịch",
                FinanceAccountType.JAR,
                "VND",
                new BigDecimal("500000")
        )));
    }

    @Test
    void rejectsArchivingMainPool() {
        UUID accountId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        FinanceAccount mainPool = account(accountId, FinanceAccountType.MAIN_POOL, "1000000.0000");
        when(accountRepository.findByIdAndOwnerId(accountId, OWNER_ID)).thenReturn(Optional.of(mainPool));

        assertFinanceAccountInvalid(() -> service().archive(OWNER_ID, accountId, "Not allowed"));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void rejectsArchivingJarUntilItsBalanceIsZero() {
        UUID accountId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        FinanceAccount jar = account(accountId, FinanceAccountType.JAR, "500000.0000");
        when(accountRepository.findByIdAndOwnerId(accountId, OWNER_ID)).thenReturn(Optional.of(jar));

        assertFinanceAccountInvalid(() -> service().archive(OWNER_ID, accountId, "Still funded"));

        verify(accountRepository, never()).save(any());
    }

    private FinanceAccountServiceImpl service() {
        return new FinanceAccountServiceImpl(accountRepository, historyRecorder);
    }

    private static FinanceAccount account(UUID id, FinanceAccountType accountType, String balance) {
        FinanceAccount account = FinanceAccount.create(
                OWNER_ID,
                OWNER_ID,
                accountType == FinanceAccountType.MAIN_POOL ? "Ví tổng" : "Hũ",
                accountType,
                "VND",
                new BigDecimal(balance)
        );
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }

    private static void assertFinanceAccountInvalid(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(FinanceErrorCode.FINANCE_ACCOUNT_INVALID);
    }
}
