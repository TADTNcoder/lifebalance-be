package com.lifebalance.finance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.finance.domain.FinanceTransactionStatus;
import com.lifebalance.finance.domain.FinanceIncomeSourceType;
import com.lifebalance.finance.domain.FinanceTransactionType;
import com.lifebalance.finance.dto.CreateTransactionRequest;
import com.lifebalance.finance.dto.TransactionResponse;
import com.lifebalance.finance.service.FinancialTransactionService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(FinancialTransactionController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        FinancialTransactionControllerTest.TestSecuritySupport.class
})
class FinancialTransactionControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOURCE_ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CATEGORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TRANSACTION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID TASK_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final OffsetDateTime TRANSACTION_DATE = OffsetDateTime.parse("2026-08-21T08:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialTransactionService transactionService;

    @Test
    void createTransactionReturnsCreatedAndDelegatesAuthenticatedOwnerRequest() throws Exception {
        when(transactionService.create(eq(OWNER_ID), any(CreateTransactionRequest.class)))
                .thenReturn(new TransactionResponse(
                        TRANSACTION_ID,
                        OWNER_ID,
                        FinanceTransactionType.EXPENSE,
                        FinanceTransactionStatus.POSTED,
                        SOURCE_ACCOUNT_ID,
                        "Daily wallet",
                        null,
                        null,
                        CATEGORY_ID,
                        "Food",
                        new BigDecimal("125.5000"),
                        "USD",
                        TRANSACTION_DATE,
                        "Bữa trưa nhóm",
                        "Team lunch",
                        null,
                        null,
                        null,
                        null,
                        null,
                        OWNER_ID,
                        OWNER_ID,
                        TRANSACTION_DATE,
                        TRANSACTION_DATE
                ));

        mockMvc.perform(post("/api/transactions")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionType": "EXPENSE",
                                  "sourceAccountId": "%s",
                                  "categoryId": "%s",
                                  "amount": 125.5000,
                                  "currencyCode": "USD",
                                  "transactionDate": "2026-08-21T08:30:00Z",
                                  "transactionName": "Bữa trưa nhóm",
                                  "description": "Team lunch",
                                  "reason": "Lunch budget"
                                }
                                """.formatted(SOURCE_ACCOUNT_ID, CATEGORY_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(TRANSACTION_ID.toString()))
                .andExpect(jsonPath("$.data.transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$.data.status").value("POSTED"))
                .andExpect(jsonPath("$.data.sourceAccountId").value(SOURCE_ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.data.categoryId").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.data.amount").value(125.5000))
                .andExpect(jsonPath("$.data.currencyCode").value("USD"));

        ArgumentCaptor<CreateTransactionRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateTransactionRequest.class);
        verify(transactionService).create(eq(OWNER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().transactionType()).isEqualTo(FinanceTransactionType.EXPENSE);
        assertThat(requestCaptor.getValue().sourceAccountId()).isEqualTo(SOURCE_ACCOUNT_ID);
        assertThat(requestCaptor.getValue().categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(requestCaptor.getValue().transactionName()).isEqualTo("Bữa trưa nhóm");
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo("125.5000");
        assertThat(requestCaptor.getValue().reason()).isEqualTo("Lunch budget");
    }

    @Test
    void createTransactionReturnsBadRequestWhenAmountIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionType": "EXPENSE",
                                  "sourceAccountId": "%s",
                                  "amount": 0.0000,
                                  "currencyCode": "USD",
                                  "transactionDate": "2026-08-21T08:30:00Z"
                                }
                                """.formatted(SOURCE_ACCOUNT_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.amount").exists());

        verify(transactionService, never()).create(any(), any());
    }

    @Test
    void createTransactionAcceptsMonthlySalaryMetadata() throws Exception {
        when(transactionService.create(eq(OWNER_ID), any(CreateTransactionRequest.class)))
                .thenReturn(new TransactionResponse(
                        TRANSACTION_ID,
                        OWNER_ID,
                        FinanceTransactionType.INCOME,
                        FinanceTransactionStatus.POSTED,
                        null,
                        null,
                        SOURCE_ACCOUNT_ID,
                        "Salary wallet",
                        CATEGORY_ID,
                        "Salary",
                        new BigDecimal("1050.0000"),
                        "USD",
                        TRANSACTION_DATE,
                        "Salary August 2026",
                        null,
                        TASK_ID,
                        null,
                        null,
                        null,
                        null,
                        OWNER_ID,
                        OWNER_ID,
                        TRANSACTION_DATE,
                        TRANSACTION_DATE,
                        FinanceIncomeSourceType.MONTHLY_SALARY,
                        "2026-08",
                        new BigDecimal("1000.0000"),
                        new BigDecimal("100.0000"),
                        new BigDecimal("50.0000")
                ));

        mockMvc.perform(post("/api/transactions")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionType": "INCOME",
                                  "destinationAccountId": "%s",
                                  "categoryId": "%s",
                                  "amount": 1050.0000,
                                  "currencyCode": "USD",
                                  "transactionDate": "2026-08-21T08:30:00Z",
                                  "transactionName": "Salary August 2026",
                                  "taskId": "%s",
                                  "incomeSourceType": "MONTHLY_SALARY",
                                  "salaryPeriod": "2026-08",
                                  "baseSalary": 1000.0000,
                                  "bonusAmount": 100.0000,
                                  "deductionAmount": 50.0000
                                }
                                """.formatted(SOURCE_ACCOUNT_ID, CATEGORY_ID, TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.incomeSourceType").value("MONTHLY_SALARY"))
                .andExpect(jsonPath("$.data.salaryPeriod").value("2026-08"))
                .andExpect(jsonPath("$.data.baseSalary").value(1000.0000))
                .andExpect(jsonPath("$.data.bonusAmount").value(100.0000))
                .andExpect(jsonPath("$.data.deductionAmount").value(50.0000));

        ArgumentCaptor<CreateTransactionRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateTransactionRequest.class);
        verify(transactionService).create(eq(OWNER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().incomeSourceType())
                .isEqualTo(FinanceIncomeSourceType.MONTHLY_SALARY);
        assertThat(requestCaptor.getValue().salaryPeriod()).isEqualTo("2026-08");
        assertThat(requestCaptor.getValue().taskId()).isEqualTo(TASK_ID);
    }

    @Test
    void createTransactionReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionType": "EXPENSE",
                                  "sourceAccountId": "%s",
                                  "amount": 10.0000,
                                  "currencyCode": "USD",
                                  "transactionDate": "2026-08-21T08:30:00Z"
                                }
                                """.formatted(SOURCE_ACCOUNT_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(transactionService, never()).create(any(), any());
    }

    private static RequestPostProcessor authenticatedUser() {
        return jwt().jwt(jwt -> jwt
                .subject("kc-user-123")
                .claim("lifebalance_user_id", OWNER_ID.toString())
        );
    }

    @TestConfiguration
    static class TestSecuritySupport {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("JWT decoding is not used by this test");
            };
        }
    }
}
