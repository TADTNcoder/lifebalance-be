package com.lifebalance.timeline.controller;

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
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import com.lifebalance.timeline.domain.TimelineConflictPolicy;
import com.lifebalance.timeline.domain.TimelinePlacementSource;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import com.lifebalance.timeline.dto.ScheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.TimelinePlacementResponse;
import com.lifebalance.timeline.service.TimelinePlacementService;
import java.time.OffsetDateTime;
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

@WebMvcTest(TimelinePlacementController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        TimelinePlacementControllerTest.TestSecuritySupport.class
})
class TimelinePlacementControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PLACEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final OffsetDateTime START_AT = OffsetDateTime.parse("2026-08-21T09:00:00Z");
    private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-08-21T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimelinePlacementService timelinePlacementService;

    @Test
    void scheduleReturnsCreatedAndDelegatesAuthenticatedOwner() throws Exception {
        when(timelinePlacementService.schedule(eq(OWNER_ID), any(ScheduleTimelinePlacementRequest.class)))
                .thenReturn(new TimelinePlacementResponse(
                        PLACEMENT_ID,
                        OWNER_ID,
                        TASK_ID,
                        "Design schedule",
                        TimelineTaskStatus.SCHEDULED,
                        START_AT,
                        END_AT,
                        "UTC",
                        TimelinePlacementSource.MANUAL,
                        TimelinePlacementStatus.ACTIVE,
                        TimelineConflictPolicy.REJECT,
                        false,
                        false,
                        null,
                        "Focus block",
                        OWNER_ID,
                        OWNER_ID,
                        START_AT,
                        START_AT
                ));

        mockMvc.perform(post("/api/timeline/placements")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "startAt": "2026-08-21T09:00:00Z",
                                  "endAt": "2026-08-21T10:00:00Z",
                                  "timezone": "UTC",
                                  "reason": "Focus block"
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(PLACEMENT_ID.toString()))
                .andExpect(jsonPath("$.data.taskId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(timelinePlacementService).schedule(eq(OWNER_ID), any(ScheduleTimelinePlacementRequest.class));
    }

    @Test
    void scheduleReturnsBadRequestWhenTaskIdMissing() throws Exception {
        mockMvc.perform(post("/api/timeline/placements")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "2026-08-21T09:00:00Z",
                                  "endAt": "2026-08-21T10:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.taskId").exists());

        verify(timelinePlacementService, never()).schedule(any(), any());
    }

    @Test
    void scheduleReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post("/api/timeline/placements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "startAt": "2026-08-21T09:00:00Z",
                                  "endAt": "2026-08-21T10:00:00Z"
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(timelinePlacementService, never()).schedule(any(), any());
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
