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

import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAllocatedCapitalException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationStateException;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
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
    private static final String RELEASE_ENDPOINT = "/api/v1/capital-allocations/{id}/release";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalAllocationService capitalAllocationService;

    @Test
    void releaseAllocationReturnsOkAndDelegatesAuthenticatedOwnerRequest() throws Exception {
        when(capitalAllocationService.releaseCapital(eq(OWNER_ID), eq(ALLOCATION_ID), any()))
                .thenReturn(releaseResponse());

        mockMvc.perform(post(RELEASE_ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 25.0000,
                                  "reason": "Release unused allocation"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.capitalType").value("TIME"))
                .andExpect(jsonPath("$.data.targetType").value("TASK"))
                .andExpect(jsonPath("$.data.targetId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.data.targetAllocatedAmount").value(55.0000))
                .andExpect(jsonPath("$.data.totalAllocatedAmount").value(55.0000))
                .andExpect(jsonPath("$.data.remainingAmount").value(45.0000))
                .andExpect(jsonPath("$.data.overAllocated").value(false))
                .andExpect(jsonPath("$.data.historyIds[0]").value(HISTORY_ID.toString()));

        ArgumentCaptor<CapitalAllocationReleaseRequest> requestCaptor =
                ArgumentCaptor.forClass(CapitalAllocationReleaseRequest.class);
        verify(capitalAllocationService).releaseCapital(eq(OWNER_ID), eq(ALLOCATION_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo("25.0000");
        assertThat(requestCaptor.getValue().reason()).isEqualTo("Release unused allocation");
    }

    @Test
    void releaseAllocationReturnsBadRequestWhenAmountIsNotPositive() throws Exception {
        mockMvc.perform(post(RELEASE_ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0.0000,
                                  "reason": "Invalid release"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.amount").exists());

        verify(capitalAllocationService, never()).releaseCapital(any(), any(), any());
    }

    @Test
    void releaseAllocationReturnsBadRequestWhenAmountExceedsEffectiveAllocation() throws Exception {
        when(capitalAllocationService.releaseCapital(eq(OWNER_ID), eq(ALLOCATION_ID), any()))
                .thenThrow(new InsufficientAllocatedCapitalException(
                        CYCLE_ID,
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        TASK_ID,
                        new BigDecimal("90.0000"),
                        new BigDecimal("30.0000")
                ));

        mockMvc.perform(post(RELEASE_ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 90.0000,
                                  "reason": "Too much"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InsufficientAllocatedCapitalException.ERROR_CODE));

        verify(capitalAllocationService).releaseCapital(eq(OWNER_ID), eq(ALLOCATION_ID), any());
    }

    @Test
    void releaseAllocationReturnsConflictWhenAllocationIsNoLongerActive() throws Exception {
        when(capitalAllocationService.releaseCapital(eq(OWNER_ID), eq(ALLOCATION_ID), any()))
                .thenThrow(new InvalidAllocationStateException(
                        ALLOCATION_ID,
                        AllocationStatus.RELEASED,
                        "release capital"
                ));

        mockMvc.perform(post(RELEASE_ENDPOINT, ALLOCATION_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.0000,
                                  "reason": "Already released"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidAllocationStateException.ERROR_CODE));

        verify(capitalAllocationService).releaseCapital(eq(OWNER_ID), eq(ALLOCATION_ID), any());
    }

    @Test
    void releaseAllocationReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post(RELEASE_ENDPOINT, ALLOCATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.0000,
                                  "reason": "Missing auth"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalAllocationService, never()).releaseCapital(any(), any(), any());
    }

    private static AllocationResponse releaseResponse() {
        return new AllocationResponse(
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                TASK_ID,
                new BigDecimal("55.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("55.0000"),
                new BigDecimal("45.0000"),
                false,
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
