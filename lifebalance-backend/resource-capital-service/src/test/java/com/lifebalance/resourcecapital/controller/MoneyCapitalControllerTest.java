package com.lifebalance.resourcecapital.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.service.CapitalService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(MoneyCapitalController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        MoneyCapitalControllerTest.TestSecuritySupport.class
})
class MoneyCapitalControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MONEY_CAPITAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String ENDPOINT = "/api/v1/capital-cycles/{cycleId}/money-capital";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapitalService capitalService;

    @Test
    void setupReturnsCreatedWhenRequestIsValidAndUserIsAuthenticated() throws Exception {
        SetupMoneyCapitalRequest request = setupRequest();
        MoneyCapitalResponse response = response(new BigDecimal("15000000.0000"));

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/capital-cycles/" + CYCLE_ID + "/money-capital"))
                .andExpect(jsonPath("$.id").value(MONEY_CAPITAL_ID.toString()))
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.plannedAmount").value(15000000.0000))
                .andExpect(jsonPath("$.allocatedAmount").value(0.0000))
                .andExpect(jsonPath("$.availableAmount").value(15000000.0000))
                .andExpect(jsonPath("$.remainingAmount").value(15000000.0000))
                .andExpect(jsonPath("$.currencyCode").value("VND"))
                .andExpect(jsonPath("$.initialized").value(true));

        ArgumentCaptor<SetupMoneyCapitalRequest> requestCaptor =
                ArgumentCaptor.forClass(SetupMoneyCapitalRequest.class);
        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().plannedAmount()).isEqualByComparingTo("15000000");
        assertThat(requestCaptor.getValue().currencyCode()).isEqualTo("VND");
    }

    @Test
    void setupReturnsCreatedWhenPlannedAmountIsZero() throws Exception {
        SetupMoneyCapitalRequest request = new SetupMoneyCapitalRequest(BigDecimal.ZERO, "VND");
        MoneyCapitalResponse response = response(new BigDecimal("0.0000"));

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plannedAmount").value(0.0000))
                .andExpect(jsonPath("$.availableAmount").value(0.0000))
                .andExpect(jsonPath("$.remainingAmount").value(0.0000))
                .andExpect(jsonPath("$.currencyCode").value("VND"))
                .andExpect(jsonPath("$.initialized").value(true));

        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class));
    }

    @Test
    void setupReturnsCreatedWhenPlannedAmountIsAtSupportedPrecisionBoundary() throws Exception {
        SetupMoneyCapitalRequest request = new SetupMoneyCapitalRequest(
                new BigDecimal("999999999999999.9999"),
                "VND"
        );
        MoneyCapitalResponse response = response(new BigDecimal("999999999999999.9999"));

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plannedAmount").value(999999999999999.9999))
                .andExpect(jsonPath("$.availableAmount").value(999999999999999.9999))
                .andExpect(jsonPath("$.remainingAmount").value(999999999999999.9999))
                .andExpect(jsonPath("$.currencyCode").value("VND"))
                .andExpect(jsonPath("$.initialized").value(true));

        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class));
    }

    @Test
    void setupReturnsBadRequestWhenPlannedAmountIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currencyCode": "VND"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedAmount").exists());

        verifyNoInteractions(capitalService);
    }

    @Test
    void setupReturnsBadRequestWhenPlannedAmountIsNegative() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plannedAmount": -0.01,
                                  "currencyCode": "VND"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedAmount").exists());

        verifyNoInteractions(capitalService);
    }

    @Test
    void setupReturnsBadRequestWhenPlannedAmountFractionExceedsSupportedScale() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plannedAmount": 1.00001,
                                  "currencyCode": "VND"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedAmount").exists());

        verify(capitalService, never()).setupMoneyCapital(any(), any(), any());
    }

    @Test
    void setupReturnsBadRequestWhenPlannedAmountIntegerDigitsExceedSupportedPrecision() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plannedAmount": 1000000000000000.0000,
                                  "currencyCode": "VND"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedAmount").exists());

        verify(capitalService, never()).setupMoneyCapital(any(), any(), any());
    }

    @Test
    void setupReturnsBadRequestWhenCurrencyCodeIsInvalid() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plannedAmount": 15000000,
                                  "currencyCode": "VN1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.currencyCode").exists());

        verifyNoInteractions(capitalService);
    }

    @Test
    void setupReturnsBadRequestWhenCycleStatusDoesNotAllowSetup() throws Exception {
        SetupMoneyCapitalRequest request = setupRequest();

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenThrow(new InvalidCapitalCycleStateException(
                        CYCLE_ID,
                        CapitalCycleStatus.ACTIVE,
                        "initialize money capital",
                        "money capital initialization is allowed only while the cycle accepts initial capital setup"
                ));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCycleStateException.ERROR_CODE));

        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class));
    }

    @Test
    void setupReturnsConflictWhenMoneyCapitalAlreadyExists() throws Exception {
        SetupMoneyCapitalRequest request = setupRequest();

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenThrow(new CapitalAlreadyInitializedException(CYCLE_ID, CapitalKind.MONEY));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MONEY_CAPITAL_ALREADY_EXISTS"));

        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class));
    }

    @Test
    void setupReturnsNotFoundWhenCycleDoesNotExistOrBelongToUser() throws Exception {
        SetupMoneyCapitalRequest request = setupRequest();

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE));

        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class));
    }

    @Test
    void setupReturnsForbiddenWhenAccessIsDenied() throws Exception {
        SetupMoneyCapitalRequest request = setupRequest();

        when(capitalService.setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class)))
                .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(capitalService).setupMoneyCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupMoneyCapitalRequest.class));
    }

    @Test
    void setupReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalService, never()).setupMoneyCapital(any(), any(), any());
    }

    @Test
    void setupReturnsUnauthorizedWhenInternalUserIdClaimIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalService, never()).setupMoneyCapital(any(), any(), any());
    }

    private static SetupMoneyCapitalRequest setupRequest() {
        return new SetupMoneyCapitalRequest(new BigDecimal("15000000"), "VND");
    }

    private static MoneyCapitalResponse response(BigDecimal plannedAmount) {
        return new MoneyCapitalResponse(
                MONEY_CAPITAL_ID,
                CYCLE_ID,
                plannedAmount,
                BigDecimal.ZERO.setScale(4),
                plannedAmount,
                plannedAmount,
                "VND",
                true
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
