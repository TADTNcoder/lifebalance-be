package com.lifebalance.resourcecapital.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAllocationChangeRequest;
import com.lifebalance.resourcecapital.service.CapitalAllocationService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.util.List;
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

@WebMvcTest(CapitalAllocationController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        CapitalAllocationControllerTest.TestSecuritySupport.class
})
class CapitalAllocationControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ALLOCATION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID HISTORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String ENDPOINT = "/api/v1/capital-allocations/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapitalAllocationService capitalAllocationService;

    @Test
    void changeAllocationReturnsUpdatedAmountWhenRequestIsValidAndUserIsAuthenticated() throws Exception {
        CapitalAllocationChangeRequest request = new CapitalAllocationChangeRequest(
                new BigDecimal("120.0000"),
                true,
                "Approved update"
        );
        when(capitalAllocationService.changeAllocation(
                eq(OWNER_ID),
                eq(ALLOCATION_ID),
                any(CapitalAllocationChangeRequest.class)
        )).thenReturn(response());

        mockMvc.perform(patch(ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.capitalType").value("TIME"))
                .andExpect(jsonPath("$.data.targetType").value("TASK"))
                .andExpect(jsonPath("$.data.targetId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.data.targetAllocatedAmount").value(120.0000))
                .andExpect(jsonPath("$.data.plannedAmount").value(100.0000))
                .andExpect(jsonPath("$.data.totalAllocatedAmount").value(120.0000))
                .andExpect(jsonPath("$.data.remainingAmount").value(-20.0000))
                .andExpect(jsonPath("$.data.overAllocated").value(true))
                .andExpect(jsonPath("$.data.historyIds[0]").value(HISTORY_ID.toString()));

        ArgumentCaptor<CapitalAllocationChangeRequest> requestCaptor =
                ArgumentCaptor.forClass(CapitalAllocationChangeRequest.class);
        verify(capitalAllocationService).changeAllocation(eq(OWNER_ID), eq(ALLOCATION_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().newAmount()).isEqualByComparingTo("120.0000");
        assertThat(requestCaptor.getValue().overAllocationConfirmed()).isTrue();
        assertThat(requestCaptor.getValue().reason()).isEqualTo("Approved update");
    }

    @Test
    void changeAllocationReturnsConflictWhenOverAllocationNeedsConfirmation() throws Exception {
        CapitalAllocationChangeRequest request = new CapitalAllocationChangeRequest(
                new BigDecimal("120.0000"),
                false,
                "Needs confirmation"
        );
        when(capitalAllocationService.changeAllocation(
                eq(OWNER_ID),
                eq(ALLOCATION_ID),
                any(CapitalAllocationChangeRequest.class)
        )).thenThrow(new OverAllocationConfirmationRequiredException(
                CYCLE_ID,
                CapitalKind.TIME,
                new BigDecimal("10.0000"),
                new BigDecimal("30.0000"),
                new BigDecimal("-20.0000")
        ));

        mockMvc.perform(patch(ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(OverAllocationConfirmationRequiredException.ERROR_CODE));

        verify(capitalAllocationService).changeAllocation(
                eq(OWNER_ID),
                eq(ALLOCATION_ID),
                any(CapitalAllocationChangeRequest.class)
        );
    }

    @Test
    void changeAllocationReturnsBadRequestWhenNewAmountIsNegative() throws Exception {
        mockMvc.perform(patch(ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newAmount": -1,
                                  "overAllocationConfirmed": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.newAmount").exists());

        verifyNoInteractions(capitalAllocationService);
    }

    @Test
    void changeAllocationReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(patch(ENDPOINT, ALLOCATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalAllocationService, never()).changeAllocation(any(), any(), any());
    }

    @Test
    void changeAllocationReturnsUnauthorizedWhenInternalUserIdClaimIsMissing() throws Exception {
        mockMvc.perform(patch(ENDPOINT, ALLOCATION_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalAllocationService, never()).changeAllocation(any(), any(), any());
    }

    private static CapitalAllocationChangeRequest validRequest() {
        return new CapitalAllocationChangeRequest(new BigDecimal("80.0000"), false, "Resize");
    }

    private static AllocationResponse response() {
        return new AllocationResponse(
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                TASK_ID,
                new BigDecimal("120.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("-20.0000"),
                true,
                List.of(HISTORY_ID)
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
