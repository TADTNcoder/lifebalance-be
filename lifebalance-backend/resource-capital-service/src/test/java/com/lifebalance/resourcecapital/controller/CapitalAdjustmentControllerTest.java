package com.lifebalance.resourcecapital.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
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

@WebMvcTest(CapitalAdjustmentController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        CapitalAdjustmentControllerTest.TestSecuritySupport.class
})
class CapitalAdjustmentControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID HISTORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String MONEY_ENDPOINT = "/api/v1/capital-adjustments/money";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapitalAdjustmentService capitalAdjustmentService;

    @Test
    void adjustMoneyCapitalReturnsOkWithApiEnvelope() throws Exception {
        when(capitalAdjustmentService.adjustMoneyCapital(
                eq(OWNER_ID),
                eq(CYCLE_ID),
                any(AdjustMoneyCapitalRequest.class)
        )).thenReturn(response());

        mockMvc.perform(post(MONEY_ENDPOINT)
                        .queryParam("cycleId", CYCLE_ID.toString())
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.capitalCycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.actionType").value("ADJUSTMENT_INCREASE"))
                .andExpect(jsonPath("$.data.amount").value(50.0000))
                .andExpect(jsonPath("$.data.currencyCode").value("USD"))
                .andExpect(jsonPath("$.data.historyId").value(HISTORY_ID.toString()));

        ArgumentCaptor<AdjustMoneyCapitalRequest> requestCaptor =
                ArgumentCaptor.forClass(AdjustMoneyCapitalRequest.class);
        verify(capitalAdjustmentService).adjustMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().adjustmentType()).isEqualTo(CapitalAdjustmentType.INCREASE);
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo("50.0000");
        assertThat(requestCaptor.getValue().currencyCode()).isEqualTo("USD");
    }

    @Test
    void adjustMoneyCapitalRejectsInvalidAmountBeforeServiceCall() throws Exception {
        mockMvc.perform(post(MONEY_ENDPOINT)
                        .queryParam("cycleId", CYCLE_ID.toString())
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adjustmentType": "INCREASE",
                                  "amount": 0.0000,
                                  "reason": "No-op",
                                  "currencyCode": "USD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.amount").exists());

        verify(capitalAdjustmentService, never()).adjustMoneyCapital(any(), any(), any());
    }

    @Test
    void adjustMoneyCapitalReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(MONEY_ENDPOINT)
                        .queryParam("cycleId", CYCLE_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalAdjustmentService, never()).adjustMoneyCapital(any(), any(), any());
    }

    private static AdjustMoneyCapitalRequest request() {
        return new AdjustMoneyCapitalRequest(
                CapitalAdjustmentType.INCREASE,
                new BigDecimal("50.0000"),
                "Monthly top up",
                "USD",
                false,
                null
        );
    }

    private static MoneyCapitalAdjustmentResponse response() {
        return new MoneyCapitalAdjustmentResponse(
                CYCLE_ID,
                CapitalActionType.ADJUSTMENT_INCREASE,
                new BigDecimal("50.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("150.0000"),
                "USD",
                "Monthly top up",
                HISTORY_ID
        );
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
