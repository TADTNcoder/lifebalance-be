package com.lifebalance.analytics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import com.lifebalance.analytics.dto.ActualRecordResponse;
import com.lifebalance.analytics.dto.RecordActualRequest;
import com.lifebalance.analytics.service.ActualRecordService;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(ActualRecordController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        ActualRecordControllerTest.TestSecuritySupport.class
})
class ActualRecordControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACTUAL_RECORD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-21T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActualRecordService actualRecordService;

    @Test
    void recordReturnsCreatedAndDelegatesAuthenticatedOwner() throws Exception {
        when(actualRecordService.record(eq(OWNER_ID), any(RecordActualRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/analytics/actual-records")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recordType": "TIME",
                                  "taskId": "%s",
                                  "actualMinutes": 90,
                                  "actualDate": "2026-08-21",
                                  "note": "Deep work",
                                  "source": "manual"
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(ACTUAL_RECORD_ID.toString()))
                .andExpect(jsonPath("$.data.recordType").value("TIME"))
                .andExpect(jsonPath("$.data.actualMinutes").value(90));

        verify(actualRecordService).record(eq(OWNER_ID), any(RecordActualRequest.class));
    }

    @Test
    void recordReturnsBadRequestWhenRecordTypeMissing() throws Exception {
        mockMvc.perform(post("/api/analytics/actual-records")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "actualMinutes": 90,
                                  "actualDate": "2026-08-21"
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.recordType").exists());

        verify(actualRecordService, never()).record(any(), any());
    }

    @Test
    void recordReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post("/api/analytics/actual-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recordType": "TIME",
                                  "taskId": "%s",
                                  "actualMinutes": 90,
                                  "actualDate": "2026-08-21"
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(actualRecordService, never()).record(any(), any());
    }

    private static ActualRecordResponse response() {
        return new ActualRecordResponse(
                ACTUAL_RECORD_ID,
                OWNER_ID,
                OWNER_ID,
                TASK_ID,
                null,
                null,
                Set.of(),
                ActualRecordType.TIME,
                ActualRecordStatus.ACTIVE,
                90,
                null,
                null,
                LocalDate.parse("2026-08-21"),
                NOW,
                "Deep work",
                "manual",
                null,
                OWNER_ID,
                OWNER_ID,
                NOW,
                NOW
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
