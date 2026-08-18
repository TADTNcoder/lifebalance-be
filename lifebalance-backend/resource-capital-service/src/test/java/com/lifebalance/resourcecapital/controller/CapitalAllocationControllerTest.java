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
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationTargetException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
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
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PROJECT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID HISTORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String ALLOCATE_ENDPOINT = "/api/v1/capital-allocations";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalAllocationService capitalAllocationService;

    @Test
    void allocateTimeCapitalReturnsCreatedAndDelegatesAuthenticatedOwnerRequest() throws Exception {
        when(capitalAllocationService.allocateCapital(eq(OWNER_ID), any(CreateCapitalAllocationRequest.class)))
                .thenReturn(new AllocationResponse(
                        CYCLE_ID,
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        TASK_ID,
                        new BigDecimal("45.0000"),
                        new BigDecimal("120.0000"),
                        new BigDecimal("45.0000"),
                        new BigDecimal("75.0000"),
                        false,
                        List.of(HISTORY_ID)
                ));

        mockMvc.perform(post(ALLOCATE_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 45.0000,
                                  "allowOverAllocation": false,
                                  "reason": "Initial task budget"
                                }
                                """.formatted(CYCLE_ID, TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.capitalType").value("TIME"))
                .andExpect(jsonPath("$.data.targetType").value("TASK"))
                .andExpect(jsonPath("$.data.targetId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.data.targetAllocatedAmount").value(45.0000))
                .andExpect(jsonPath("$.data.remainingAmount").value(75.0000))
                .andExpect(jsonPath("$.data.overAllocated").value(false))
                .andExpect(jsonPath("$.data.historyIds[0]").value(HISTORY_ID.toString()));

        ArgumentCaptor<CreateCapitalAllocationRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateCapitalAllocationRequest.class);
        verify(capitalAllocationService).allocateCapital(eq(OWNER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().capitalCycleId()).isEqualTo(CYCLE_ID);
        assertThat(requestCaptor.getValue().capitalType()).isEqualTo(CapitalKind.TIME);
        assertThat(requestCaptor.getValue().targetType()).isEqualTo(AllocationTargetType.TASK);
        assertThat(requestCaptor.getValue().targetId()).isEqualTo(TASK_ID);
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo("45.0000");
        assertThat(requestCaptor.getValue().allowOverAllocation()).isFalse();
        assertThat(requestCaptor.getValue().reason()).isEqualTo("Initial task budget");
    }

    @Test
    void allocateMoneyCapitalAcceptsResourceTypeAndOverAllocationConfirmationAliases() throws Exception {
        when(capitalAllocationService.allocateCapital(eq(OWNER_ID), any(CreateCapitalAllocationRequest.class)))
                .thenReturn(new AllocationResponse(
                        CYCLE_ID,
                        CapitalKind.MONEY,
                        AllocationTargetType.PROJECT,
                        PROJECT_ID,
                        new BigDecimal("750000.0000"),
                        new BigDecimal("500000.0000"),
                        new BigDecimal("750000.0000"),
                        new BigDecimal("-250000.0000"),
                        true,
                        List.of(HISTORY_ID)
                ));

        mockMvc.perform(post(ALLOCATE_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "resourceType": "MONEY",
                                  "targetType": "PROJECT",
                                  "projectId": "%s",
                                  "amount": 750000.0000,
                                  "overAllocationConfirmed": true,
                                  "reason": "Approved project spend"
                                }
                                """.formatted(CYCLE_ID, PROJECT_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.capitalType").value("MONEY"))
                .andExpect(jsonPath("$.data.targetType").value("PROJECT"))
                .andExpect(jsonPath("$.data.targetId").value(PROJECT_ID.toString()))
                .andExpect(jsonPath("$.data.remainingAmount").value(-250000.0000))
                .andExpect(jsonPath("$.data.overAllocated").value(true));

        ArgumentCaptor<CreateCapitalAllocationRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateCapitalAllocationRequest.class);
        verify(capitalAllocationService).allocateCapital(eq(OWNER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().capitalType()).isEqualTo(CapitalKind.MONEY);
        assertThat(requestCaptor.getValue().projectId()).isEqualTo(PROJECT_ID);
        assertThat(requestCaptor.getValue().allowOverAllocation()).isTrue();
    }

    @Test
    void allocateCapitalReturnsBadRequestWhenAmountIsNotPositive() throws Exception {
        mockMvc.perform(post(ALLOCATE_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 0.0000
                                }
                                """.formatted(CYCLE_ID, TASK_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.amount").exists());

        verify(capitalAllocationService, never())
                .allocateCapital(any(UUID.class), any(CreateCapitalAllocationRequest.class));
    }

    @Test
    void allocateCapitalReturnsConflictWhenOverAllocationNeedsConfirmation() throws Exception {
        when(capitalAllocationService.allocateCapital(eq(OWNER_ID), any(CreateCapitalAllocationRequest.class)))
                .thenThrow(new OverAllocationConfirmationRequiredException(
                        CYCLE_ID,
                        CapitalKind.TIME,
                        new BigDecimal("10.0000"),
                        new BigDecimal("20.0000"),
                        new BigDecimal("-10.0000")
                ));

        mockMvc.perform(post(ALLOCATE_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 20.0000
                                }
                                """.formatted(CYCLE_ID, TASK_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(OverAllocationConfirmationRequiredException.ERROR_CODE));

        verify(capitalAllocationService).allocateCapital(eq(OWNER_ID), any(CreateCapitalAllocationRequest.class));
    }

    @Test
    void allocateCapitalReturnsBadRequestWhenTargetIsInvalid() throws Exception {
        when(capitalAllocationService.allocateCapital(eq(OWNER_ID), any(CreateCapitalAllocationRequest.class)))
                .thenThrow(new InvalidAllocationTargetException("Allocation target id is required."));

        mockMvc.perform(post(ALLOCATE_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "amount": 20.0000
                                }
                                """.formatted(CYCLE_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidAllocationTargetException.ERROR_CODE));

        verify(capitalAllocationService).allocateCapital(eq(OWNER_ID), any(CreateCapitalAllocationRequest.class));
    }

    @Test
    void allocateCapitalReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post(ALLOCATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 20.0000
                                }
                                """.formatted(CYCLE_ID, TASK_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalAllocationService, never())
                .allocateCapital(any(UUID.class), any(CreateCapitalAllocationRequest.class));
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
