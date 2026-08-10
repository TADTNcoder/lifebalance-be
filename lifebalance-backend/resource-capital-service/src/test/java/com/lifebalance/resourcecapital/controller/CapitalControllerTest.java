package com.lifebalance.resourcecapital.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.dto.CapitalHistoryResponseDTO;
import com.lifebalance.resourcecapital.dto.CapitalSummaryResponseDTO;
import com.lifebalance.resourcecapital.dto.HistoryFilterRequest;
import com.lifebalance.resourcecapital.service.CapitalHistoryService;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebMvcTest(CapitalController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        CapitalControllerTest.TestSecuritySupport.class
})
class CapitalControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID HISTORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant FROM_DATE = Instant.parse("2026-08-01T00:00:00Z");
    private static final Instant TO_DATE = Instant.parse("2026-08-02T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalHistoryService capitalHistoryService;

    @MockitoBean
    private CapitalService capitalService;

    @Test
    void getHistoryReturnsPagedApiResponseAndPassesFilters() throws Exception {
        when(capitalHistoryService.getHistory(eq(OWNER_ID), eq(CYCLE_ID), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(history()),
                        PageRequest.of(0, 20),
                        1
                ));

        mockMvc.perform(get("/api/v1/capital/history")
                        .queryParam("capitalCycleId", CYCLE_ID.toString())
                        .queryParam("capitalType", "TIME")
                        .queryParam("fromDate", FROM_DATE.toString())
                        .queryParam("toDate", TO_DATE.toString())
                        .queryParam("page", "0")
                        .queryParam("size", "20")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(HISTORY_ID.toString()))
                .andExpect(jsonPath("$.data.content[0].capitalCycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.content[0].capitalType").value("TIME"))
                .andExpect(jsonPath("$.data.content[0].actionType").value("ALLOCATE"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        ArgumentCaptor<HistoryFilterRequest> filterCaptor = ArgumentCaptor.forClass(HistoryFilterRequest.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(capitalHistoryService).getHistory(
                eq(OWNER_ID),
                eq(CYCLE_ID),
                filterCaptor.capture(),
                pageableCaptor.capture()
        );
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().capitalType()).isEqualTo(CapitalKind.TIME);
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().from()).isEqualTo(FROM_DATE);
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().to()).isEqualTo(TO_DATE);
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void getSummaryReturnsApiResponse() throws Exception {
        when(capitalService.getCapitalSummary(OWNER_ID)).thenReturn(summary());

        mockMvc.perform(get("/api/v1/capital/summary").with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeCyclePresent").value(true))
                .andExpect(jsonPath("$.data.activeCycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.data.activeCycleStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.timeCapital.allocatedHours").value(8.0000))
                .andExpect(jsonPath("$.data.timeCapital.spentHours").value(2.0000))
                .andExpect(jsonPath("$.data.timeCapital.remainingHours").value(6.0000))
                .andExpect(jsonPath("$.data.moneyCapital.allocatedAmount").value(1000.0000))
                .andExpect(jsonPath("$.data.moneyCapital.spentAmount").value(250.0000))
                .andExpect(jsonPath("$.data.moneyCapital.remainingAmount").value(750.0000))
                .andExpect(jsonPath("$.data.moneyCapital.currencyCode").value("VND"));

        verify(capitalService).getCapitalSummary(OWNER_ID);
    }

    @Test
    void getHistoryReturnsBadRequestForInvalidCapitalType() throws Exception {
        mockMvc.perform(get("/api/v1/capital/history")
                        .queryParam("capitalType", "INVALID")
                        .with(authenticatedUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.capitalType").exists());

        verify(capitalHistoryService, never()).getHistory(any(), any(), any(), any());
    }

    @Test
    void getSummaryReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(get("/api/v1/capital/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalService, never()).getCapitalSummary(any());
    }

    private static CapitalHistoryResponseDTO history() {
        return new CapitalHistoryResponseDTO(
                HISTORY_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                new BigDecimal("120.0000"),
                new BigDecimal("0.0000"),
                new BigDecimal("120.0000"),
                "Plan focus task",
                "Allocate focus work time",
                CapitalReferenceType.TASK,
                TASK_ID,
                CapitalActorType.USER,
                OWNER_ID,
                FROM_DATE.plusSeconds(60)
        );
    }

    private static CapitalSummaryResponseDTO summary() {
        return new CapitalSummaryResponseDTO(
                true,
                CYCLE_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                new CapitalSummaryResponseDTO.TimeCapitalSummaryDTO(
                        new BigDecimal("8.0000"),
                        new BigDecimal("2.0000"),
                        new BigDecimal("6.0000"),
                        480L,
                        120L,
                        360L,
                        true,
                        false
                ),
                new CapitalSummaryResponseDTO.MoneyCapitalSummaryDTO(
                        new BigDecimal("1000.0000"),
                        new BigDecimal("250.0000"),
                        new BigDecimal("750.0000"),
                        "VND",
                        true,
                        false
                )
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
