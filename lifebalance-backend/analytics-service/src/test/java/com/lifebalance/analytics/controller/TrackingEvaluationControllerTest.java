package com.lifebalance.analytics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.analytics.dto.TrackingEvaluationSummaryResponse;
import com.lifebalance.analytics.service.TrackingEvaluationService;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(TrackingEvaluationController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        TrackingEvaluationControllerTest.TestSecuritySupport.class
})
class TrackingEvaluationControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final LocalDate FROM = LocalDate.parse("2026-08-01");
    private static final LocalDate TO = LocalDate.parse("2026-08-31");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrackingEvaluationService trackingEvaluationService;

    @Test
    void summaryReturnsOwnerScopedTrackingEvaluationMetrics() throws Exception {
        when(trackingEvaluationService.summary(OWNER_ID, FROM, TO, "usd")).thenReturn(summary());

        mockMvc.perform(get("/api/analytics/tracking-evaluation/summary")
                        .with(authenticatedUser())
                        .param("periodStart", FROM.toString())
                        .param("periodEnd", TO.toString())
                        .param("currencyCode", "usd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ownerId").value(OWNER_ID.toString()))
                .andExpect(jsonPath("$.data.evaluatedTaskCount").value(2))
                .andExpect(jsonPath("$.data.plannedMinutes").value(180))
                .andExpect(jsonPath("$.data.currencyCode").value("USD"))
                .andExpect(jsonPath("$.data.productivityScore").value(93.8889));

        verify(trackingEvaluationService).summary(OWNER_ID, FROM, TO, "usd");
    }

    @Test
    void summaryReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(get("/api/analytics/tracking-evaluation/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(trackingEvaluationService, never()).summary(any(), any(), any(), any());
    }

    private static TrackingEvaluationSummaryResponse summary() {
        return new TrackingEvaluationSummaryResponse(
                OWNER_ID,
                FROM,
                TO,
                "USD",
                2,
                2,
                3,
                0,
                1,
                1,
                0,
                180,
                150,
                -30,
                new BigDecimal("100.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("20.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("83.3333"),
                new BigDecimal("101.6667"),
                new BigDecimal("81.6667"),
                new BigDecimal("93.8889"),
                new BigDecimal("100.0000")
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
