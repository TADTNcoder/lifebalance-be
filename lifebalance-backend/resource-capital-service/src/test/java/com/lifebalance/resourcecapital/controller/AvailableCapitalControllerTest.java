package com.lifebalance.resourcecapital.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAllocationDataIntegrityException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceSummaryDto;
import com.lifebalance.resourcecapital.service.CapitalBalanceService;
import com.lifebalance.resourcecapital.service.mapper.AvailableCapitalMapper;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(AvailableCapitalController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        AvailableCapitalMapper.class,
        AvailableCapitalControllerTest.TestSecuritySupport.class
})
class AvailableCapitalControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String ENDPOINT = "/api/v1/capital-cycles/{cycleId}/available-capital";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalBalanceService capitalBalanceService;

    @Test
    void getAvailableCapitalReturnsBothResources() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(balance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital.availableMinutes").value(7200))
                .andExpect(jsonPath("$.timeCapital.initialized").value(true))
                .andExpect(jsonPath("$.moneyCapital.availableAmount").value(10000000.0000))
                .andExpect(jsonPath("$.moneyCapital.currencyCode").value("VND"))
                .andExpect(jsonPath("$.moneyCapital.initialized").value(true));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getAvailableCapitalReturnsTimeOnlyWhenTypeIsTime() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(balance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .queryParam("type", "TIME")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital.availableMinutes").value(7200))
                .andExpect(jsonPath("$.timeCapital.initialized").value(true))
                .andExpect(jsonPath("$.moneyCapital").value(nullValue()));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getAvailableCapitalReturnsMoneyOnlyWhenTypeIsMoney() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(balance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .queryParam("type", "MONEY")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital").value(nullValue()))
                .andExpect(jsonPath("$.moneyCapital.availableAmount").value(10000000.0000))
                .andExpect(jsonPath("$.moneyCapital.currencyCode").value("VND"))
                .andExpect(jsonPath("$.moneyCapital.initialized").value(true));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getAvailableCapitalReturnsBadRequestForInvalidType() throws Exception {
        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .queryParam("type", "INVALID")
                        .with(authenticatedUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.type").exists());

        verify(capitalBalanceService, never()).getCycleBalance(any(), any());
    }

    @Test
    void getAvailableCapitalReturnsNotFoundForForeignCycle() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getAvailableCapitalReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(get(ENDPOINT, CYCLE_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalBalanceService, never()).getCycleBalance(any(), any());
    }

    @Test
    void getAvailableCapitalReturnsUnauthorizedWhenInternalUserIdMissing() throws Exception {
        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalBalanceService, never()).getCycleBalance(any(), any());
    }

    @Test
    void getAvailableCapitalReturnsUninitializedState() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(uninitializedBalance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital.availableMinutes").value(0))
                .andExpect(jsonPath("$.timeCapital.initialized").value(false))
                .andExpect(jsonPath("$.moneyCapital.availableAmount").value(0.0000))
                .andExpect(jsonPath("$.moneyCapital.currencyCode").value(nullValue()))
                .andExpect(jsonPath("$.moneyCapital.initialized").value(false));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getAvailableCapitalReturnsTypedIntegrityErrorWhenTimeBalanceIsFractional() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(fractionalTimeBalance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalAllocationDataIntegrityException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value(
                        "TIME allocation data has an invalid persisted format for capital cycle " + CYCLE_ID + "."
                ));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    private static CapitalBalanceResponse balance() {
        return new CapitalBalanceResponse(
                CYCLE_ID,
                CapitalCycleStatus.ACTIVE,
                new CapitalBalanceSummaryDto(
                        CapitalKind.TIME,
                        money("9600.0000"),
                        money("2400.0000"),
                        money("7200.0000"),
                        money("7200.0000"),
                        money("25.00"),
                        false,
                        null,
                        true
                ),
                new CapitalBalanceSummaryDto(
                        CapitalKind.MONEY,
                        money("15000000.0000"),
                        money("5000000.0000"),
                        money("10000000.0000"),
                        money("10000000.0000"),
                        money("33.33"),
                        false,
                        "VND",
                        true
                )
        );
    }

    private static CapitalBalanceResponse uninitializedBalance() {
        return new CapitalBalanceResponse(
                CYCLE_ID,
                CapitalCycleStatus.DRAFT,
                new CapitalBalanceSummaryDto(
                        CapitalKind.TIME,
                        money("0.0000"),
                        money("0.0000"),
                        money("0.0000"),
                        money("0.0000"),
                        money("0.00"),
                        false,
                        null,
                        false
                ),
                new CapitalBalanceSummaryDto(
                        CapitalKind.MONEY,
                        money("0.0000"),
                        money("0.0000"),
                        money("0.0000"),
                        money("0.0000"),
                        money("0.00"),
                        false,
                        null,
                        false
                )
        );
    }

    private static CapitalBalanceResponse fractionalTimeBalance() {
        return new CapitalBalanceResponse(
                CYCLE_ID,
                CapitalCycleStatus.ACTIVE,
                new CapitalBalanceSummaryDto(
                        CapitalKind.TIME,
                        money("9600.0000"),
                        money("2399.5000"),
                        money("7200.5000"),
                        money("7200.5000"),
                        money("25.00"),
                        false,
                        null,
                        true
                ),
                new CapitalBalanceSummaryDto(
                        CapitalKind.MONEY,
                        money("15000000.0000"),
                        money("5000000.0000"),
                        money("10000000.0000"),
                        money("10000000.0000"),
                        money("33.33"),
                        false,
                        "VND",
                        true
                )
        );
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
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
