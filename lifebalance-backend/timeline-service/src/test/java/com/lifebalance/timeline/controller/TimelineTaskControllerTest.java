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
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import com.lifebalance.timeline.dto.TimelineTaskResponse;
import com.lifebalance.timeline.dto.UpsertTimelineTaskRequest;
import com.lifebalance.timeline.service.TimelineTaskService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(TimelineTaskController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        TimelineTaskControllerTest.TestSecuritySupport.class
})
@TestPropertySource(properties = "lifebalance.integration.internal-secret=test-secret")
class TimelineTaskControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-21T09:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimelineTaskService timelineTaskService;

    @Test
    void upsertDelegatesWhenPayloadOwnerMatchesAuthenticatedOwner() throws Exception {
        when(timelineTaskService.upsertTask(eq(OWNER_ID), any(UpsertTimelineTaskRequest.class)))
                .thenReturn(new TimelineTaskResponse(
                        TASK_ID,
                        OWNER_ID,
                        "Design schedule",
                        TimelineTaskStatus.PLANNED,
                        false,
                        45,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        OWNER_ID,
                        OWNER_ID,
                        NOW,
                        NOW
                ));

        mockMvc.perform(post("/api/timeline/tasks")
                        .with(authenticatedUser())
                        .header(TimelineTaskController.INTERNAL_SECRET_HEADER, "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(syncPayload(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.data.ownerId").value(OWNER_ID.toString()));

        verify(timelineTaskService).upsertTask(eq(OWNER_ID), any(UpsertTimelineTaskRequest.class));
    }

    @Test
    void upsertRejectsWhenPayloadOwnerDiffersFromAuthenticatedOwner() throws Exception {
        mockMvc.perform(post("/api/timeline/tasks")
                        .with(authenticatedUser())
                        .header(TimelineTaskController.INTERNAL_SECRET_HEADER, "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(syncPayload(OTHER_OWNER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(timelineTaskService, never()).upsertTask(any(), any());
    }

    private static String syncPayload(UUID ownerId) {
        return """
                {
                  "ownerId": "%s",
                  "taskId": "%s",
                  "title": "Design schedule",
                  "taskStatus": "PLANNED",
                  "hasTimeCapital": false,
                  "estimatedMinutes": 45
                }
                """.formatted(ownerId, TASK_ID);
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
