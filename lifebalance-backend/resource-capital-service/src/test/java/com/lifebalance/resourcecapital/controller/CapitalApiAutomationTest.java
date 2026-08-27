package com.lifebalance.resourcecapital.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.resourcecapital.service.CapitalAllocationService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
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

/**
 * API automation tests for the Resource Capital module using MockMvc.
 *
 * <p>The scenarios are mapped to the project's Capital API test cases:
 * TC_CAPITAL_01 - allocate capital successfully,
 * TC_CAPITAL_02 - reject invalid allocation request,
 * TC_CAPITAL_03 - create capital adjustment successfully.
 *
 * <p>This is a Web/MVC slice test: controllers, validation, security filters,
 * JSON serialization and the common exception envelope are real; service-layer
 * dependencies are mocked so the API contract can be tested deterministically.
 */
@WebMvcTest({
        CapitalAllocationController.class,
        CapitalAdjustmentController.class
})
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        CapitalApiAutomationTest.TestSecuritySupport.class
})
class CapitalApiAutomationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID HISTORY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final String ALLOCATION_ENDPOINT = "/api/v1/capital-allocations";
    private static final String ADJUSTMENT_ENDPOINT = "/api/v1/capital-adjustments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalAllocationService capitalAllocationService;

    @MockitoBean
    private CapitalAdjustmentService capitalAdjustmentService;

    @Test
    @DisplayName("TC_CAPITAL_01 - POST /capital-allocations returns 201 for a valid allocation")
    void tcCapital01_allocateCapital_success() throws Exception {
        when(capitalAllocationService.allocateCapital(
                eq(OWNER_ID),
                any(CreateCapitalAllocationRequest.class)
        )).thenReturn(new AllocationResponse(
                CYCLE_ID,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                TASK_ID,
                new BigDecimal("120.0000"),
                new BigDecimal("480.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("360.0000"),
                false,
                List.of(HISTORY_ID)
        ));

        mockMvc.perform(post(ALLOCATION_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 120.0000,
                                  "allowOverAllocation": false,
                                  "reason": "Allocate time for task"
                                }
                                """.formatted(CYCLE_ID, TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.capitalType").value("TIME"))
                .andExpect(jsonPath("$.data.targetType").value("TASK"))
                .andExpect(jsonPath("$.data.targetId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.data.targetAllocatedAmount").value(120.0000))
                .andExpect(jsonPath("$.data.remainingAmount").value(360.0000))
                .andExpect(jsonPath("$.data.overAllocated").value(false))
                .andExpect(jsonPath("$.data.historyIds[0]").value(HISTORY_ID.toString()));

        ArgumentCaptor<CreateCapitalAllocationRequest> captor =
                ArgumentCaptor.forClass(CreateCapitalAllocationRequest.class);

        verify(capitalAllocationService).allocateCapital(eq(OWNER_ID), captor.capture());

        CreateCapitalAllocationRequest request = captor.getValue();
        assertThat(request.capitalCycleId()).isEqualTo(CYCLE_ID);
        assertThat(request.capitalType()).isEqualTo(CapitalKind.TIME);
        assertThat(request.targetType()).isEqualTo(AllocationTargetType.TASK);
        assertThat(request.targetId()).isEqualTo(TASK_ID);
        assertThat(request.amount()).isEqualByComparingTo("120.0000");
        assertThat(request.allowOverAllocation()).isFalse();
        assertThat(request.reason()).isEqualTo("Allocate time for task");
    }

    @Test
    @DisplayName("TC_CAPITAL_02 - missing capitalCycleId returns 400 validation error")
    void tcCapital02_allocateCapital_missingRequiredField_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ALLOCATION_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalType": "MONEY",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 500000.0000,
                                  "allowOverAllocation": false,
                                  "reason": "Invalid request without cycle id"
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.capitalCycleId").exists());

        verify(capitalAllocationService, never())
                .allocateCapital(any(UUID.class), any(CreateCapitalAllocationRequest.class));
    }

    @Test
    @DisplayName("TC_CAPITAL_03 - POST /capital-adjustments returns 201 for a valid adjustment")
    void tcCapital03_adjustCapital_success() throws Exception {
        CapitalAdjustmentResponse response = new CapitalAdjustmentResponse(
                101L,
                CYCLE_ID,
                CapitalKind.MONEY,
                CapitalAdjustmentType.INCREASE,
                CapitalActionType.ADJUSTMENT_INCREASE,
                new BigDecimal("250000.0000"),
                new BigDecimal("1000000.0000"),
                new BigDecimal("1250000.0000"),
                "Increase monthly money capital",
                HISTORY_ID,
                LocalDateTime.of(2026, 8, 21, 10, 30)
        );

        when(capitalAdjustmentService.adjustCapital(
                eq(OWNER_ID),
                any(CapitalAdjustmentRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post(ADJUSTMENT_ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "MONEY",
                                  "adjustmentType": "INCREASE",
                                  "amount": 250000.0000,
                                  "reason": "Increase monthly money capital",
                                  "allowOverAllocation": false
                                }
                                """.formatted(CYCLE_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/capital-adjustments/101"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.capitalCycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.capitalType").value("MONEY"))
                .andExpect(jsonPath("$.data.adjustmentType").value("INCREASE"))
                .andExpect(jsonPath("$.data.historyActionType").value("ADJUSTMENT_INCREASE"))
                .andExpect(jsonPath("$.data.amount").value(250000.0000))
                .andExpect(jsonPath("$.data.beforeAmount").value(1000000.0000))
                .andExpect(jsonPath("$.data.afterAmount").value(1250000.0000))
                .andExpect(jsonPath("$.data.historyId").value(HISTORY_ID.toString()));

        ArgumentCaptor<CapitalAdjustmentRequest> captor =
                ArgumentCaptor.forClass(CapitalAdjustmentRequest.class);

        verify(capitalAdjustmentService).adjustCapital(eq(OWNER_ID), captor.capture());

        CapitalAdjustmentRequest request = captor.getValue();
        assertThat(request.capitalCycleId()).isEqualTo(CYCLE_ID);
        assertThat(request.capitalType()).isEqualTo(CapitalKind.MONEY);
        assertThat(request.adjustmentType()).isEqualTo(CapitalAdjustmentType.INCREASE);
        assertThat(request.amount()).isEqualByComparingTo("250000.0000");
        assertThat(request.reason()).isEqualTo("Increase monthly money capital");
    }

    @Test
    @DisplayName("Capital write API requires authentication")
    void capitalWriteApi_withoutJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(ALLOCATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "capitalCycleId": "%s",
                                  "capitalType": "TIME",
                                  "targetType": "TASK",
                                  "targetId": "%s",
                                  "amount": 60.0000,
                                  "allowOverAllocation": false,
                                  "reason": "Security check"
                                }
                                """.formatted(CYCLE_ID, TASK_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalAllocationService, never())
                .allocateCapital(any(UUID.class), any(CreateCapitalAllocationRequest.class));
    }

    /**
     * Builds the same JWT shape used by the existing Resource Capital controller tests.
     * lifebalance_user_id is mapped by KeycloakUserMappingFilter into CURRENT_USER_ATTRIBUTE.
     */
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
                throw new JwtException("JWT decoding is not used by MockMvc jwt() tests");
            };
        }
    }
}