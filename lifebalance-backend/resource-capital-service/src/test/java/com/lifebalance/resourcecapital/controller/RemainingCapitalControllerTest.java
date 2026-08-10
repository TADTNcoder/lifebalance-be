package com.lifebalance.resourcecapital.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import com.lifebalance.resourcecapital.service.mapper.RemainingCapitalMapper;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(RemainingCapitalController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        RemainingCapitalMapper.class,
        RemainingCapitalControllerTest.TestSecuritySupport.class
})
class RemainingCapitalControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String ENDPOINT = "/api/v1/capital-cycles/{cycleId}/remaining-capital";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalBalanceService capitalBalanceService;

    @Test
    void getRemainingCapitalReturnsBothResources() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(balance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital.plannedMinutes").value(9600))
                .andExpect(jsonPath("$.timeCapital.allocatedMinutes").value(1200))
                .andExpect(jsonPath("$.timeCapital.remainingMinutes").value(8400))
                .andExpect(jsonPath("$.timeCapital.overAllocated").value(false))
                .andExpect(jsonPath("$.timeCapital.initialized").value(true))
                .andExpect(jsonPath("$.moneyCapital.plannedAmount").value(15000000.0000))
                .andExpect(jsonPath("$.moneyCapital.allocatedAmount").value(2000000.0000))
                .andExpect(jsonPath("$.moneyCapital.remainingAmount").value(13000000.0000))
                .andExpect(jsonPath("$.moneyCapital.currencyCode").value("VND"))
                .andExpect(jsonPath("$.moneyCapital.overAllocated").value(false))
                .andExpect(jsonPath("$.moneyCapital.initialized").value(true));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsTimeOnlyWhenTypeIsTime() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(balance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .queryParam("type", "TIME")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital.plannedMinutes").value(9600))
                .andExpect(jsonPath("$.timeCapital.allocatedMinutes").value(1200))
                .andExpect(jsonPath("$.timeCapital.remainingMinutes").value(8400))
                .andExpect(jsonPath("$.moneyCapital").value(nullValue()));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsMoneyOnlyWhenTypeIsMoney() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(balance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .queryParam("type", "MONEY")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital").value(nullValue()))
                .andExpect(jsonPath("$.moneyCapital.plannedAmount").value(15000000.0000))
                .andExpect(jsonPath("$.moneyCapital.allocatedAmount").value(2000000.0000))
                .andExpect(jsonPath("$.moneyCapital.remainingAmount").value(13000000.0000))
                .andExpect(jsonPath("$.moneyCapital.currencyCode").value("VND"));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsOverAllocationState() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(overAllocatedBalance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeCapital.plannedMinutes").value(600))
                .andExpect(jsonPath("$.timeCapital.allocatedMinutes").value(720))
                .andExpect(jsonPath("$.timeCapital.remainingMinutes").value(-120))
                .andExpect(jsonPath("$.timeCapital.overAllocated").value(true))
                .andExpect(jsonPath("$.moneyCapital.plannedAmount").value(15000000.0000))
                .andExpect(jsonPath("$.moneyCapital.allocatedAmount").value(17000000.0000))
                .andExpect(jsonPath("$.moneyCapital.remainingAmount").value(-2000000.0000))
                .andExpect(jsonPath("$.moneyCapital.overAllocated").value(true));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsBadRequestForInvalidType() throws Exception {
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
    void getRemainingCapitalReturnsNotFoundForForeignOrMissingCycle() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsForbiddenWhenAuthorizationLayerDeniesAccess() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID))
                .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(get(ENDPOINT, CYCLE_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalBalanceService, never()).getCycleBalance(any(), any());
    }

    @Test
    void getRemainingCapitalReturnsUnauthorizedWhenInternalUserIdMissing() throws Exception {
        mockMvc.perform(get(ENDPOINT, CYCLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalBalanceService, never()).getCycleBalance(any(), any());
    }

    @Test
    void getRemainingCapitalReturnsUninitializedState() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(uninitializedBalance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.timeCapital.plannedMinutes").value(0))
                .andExpect(jsonPath("$.timeCapital.allocatedMinutes").value(0))
                .andExpect(jsonPath("$.timeCapital.remainingMinutes").value(0))
                .andExpect(jsonPath("$.timeCapital.overAllocated").value(false))
                .andExpect(jsonPath("$.timeCapital.initialized").value(false))
                .andExpect(jsonPath("$.moneyCapital.plannedAmount").value(0.0000))
                .andExpect(jsonPath("$.moneyCapital.allocatedAmount").value(0.0000))
                .andExpect(jsonPath("$.moneyCapital.remainingAmount").value(0.0000))
                .andExpect(jsonPath("$.moneyCapital.currencyCode").value(nullValue()))
                .andExpect(jsonPath("$.moneyCapital.overAllocated").value(false))
                .andExpect(jsonPath("$.moneyCapital.initialized").value(false));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    @Test
    void getRemainingCapitalReturnsTypedIntegrityErrorWhenTimeBalanceIsFractional() throws Exception {
        when(capitalBalanceService.getCycleBalance(OWNER_ID, CYCLE_ID)).thenReturn(fractionalTimeBalance());

        mockMvc.perform(get(ENDPOINT, CYCLE_ID).with(authenticatedUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalAllocationDataIntegrityException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital allocation data is inconsistent."));

        verify(capitalBalanceService).getCycleBalance(OWNER_ID, CYCLE_ID);
    }

    private static CapitalBalanceResponse balance() {
        return new CapitalBalanceResponse(
                CYCLE_ID,
                CapitalCycleStatus.ACTIVE,
                new CapitalBalanceSummaryDto(
                        CapitalKind.TIME,
                        money("9600.0000"),
                        money("1200.0000"),
                        money("8400.0000"),
                        money("8400.0000"),
                        money("12.50"),
                        false,
                        null,
                        true
                ),
                new CapitalBalanceSummaryDto(
                        CapitalKind.MONEY,
                        money("15000000.0000"),
                        money("2000000.0000"),
                        money("13000000.0000"),
                        money("13000000.0000"),
                        money("13.33"),
                        false,
                        "VND",
                        true
                )
        );
    }

    private static CapitalBalanceResponse overAllocatedBalance() {
        return new CapitalBalanceResponse(
                CYCLE_ID,
                CapitalCycleStatus.ACTIVE,
                new CapitalBalanceSummaryDto(
                        CapitalKind.TIME,
                        money("600.0000"),
                        money("720.0000"),
                        money("-120.0000"),
                        money("-120.0000"),
                        money("120.00"),
                        true,
                        null,
                        true
                ),
                new CapitalBalanceSummaryDto(
                        CapitalKind.MONEY,
                        money("15000000.0000"),
                        money("17000000.0000"),
                        money("-2000000.0000"),
                        money("-2000000.0000"),
                        money("113.33"),
                        true,
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
                        money("1199.5000"),
                        money("8400.5000"),
                        money("8400.5000"),
                        money("12.50"),
                        false,
                        null,
                        true
                ),
                new CapitalBalanceSummaryDto(
                        CapitalKind.MONEY,
                        money("15000000.0000"),
                        money("2000000.0000"),
                        money("13000000.0000"),
                        money("13000000.0000"),
                        money("13.33"),
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
