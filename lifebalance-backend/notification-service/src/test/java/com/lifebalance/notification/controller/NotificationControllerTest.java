package com.lifebalance.notification.controller;

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
import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationPriority;
import com.lifebalance.notification.domain.NotificationStatus;
import com.lifebalance.notification.dto.CreateNotificationRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.service.NotificationService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.time.OffsetDateTime;
import java.util.List;
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

@WebMvcTest(NotificationController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        NotificationControllerTest.TestSecuritySupport.class
})
class NotificationControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID NOTIFICATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-21T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void createReturnsCreatedAndDelegatesAuthenticatedOwner() throws Exception {
        when(notificationService.create(eq(OWNER_ID), any(CreateNotificationRequest.class)))
                .thenReturn(List.of(response()));

        mockMvc.perform(post("/api/notifications")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType": "TASK_REMINDER",
                                  "channels": ["IN_APP"],
                                  "priority": "HIGH",
                                  "title": "Task starts soon",
                                  "message": "Focus block starts at 10:00.",
                                  "referenceType": "TASK",
                                  "referenceId": "%s",
                                  "purpose": "Reminder requested by user",
                                  "policyApproved": true
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(NOTIFICATION_ID.toString()))
                .andExpect(jsonPath("$.data[0].status").value("UNREAD"))
                .andExpect(jsonPath("$.data[0].deliveryStatus").value("SENT"));

        verify(notificationService).create(eq(OWNER_ID), any(CreateNotificationRequest.class));
    }

    @Test
    void createReturnsBadRequestWhenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType": "TASK_REMINDER",
                                  "message": "Focus block starts at 10:00.",
                                  "purpose": "Reminder requested by user",
                                  "policyApproved": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.title").exists());

        verify(notificationService, never()).create(any(), any());
    }

    @Test
    void createReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType": "TASK_REMINDER",
                                  "title": "Task starts soon",
                                  "message": "Focus block starts at 10:00.",
                                  "purpose": "Reminder requested by user",
                                  "policyApproved": true
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(notificationService, never()).create(any(), any());
    }

    private static NotificationResponse response() {
        return new NotificationResponse(
                NOTIFICATION_ID,
                OWNER_ID,
                OWNER_ID,
                OWNER_ID,
                NotificationEventType.TASK_REMINDER,
                NotificationChannel.IN_APP,
                NotificationPriority.HIGH,
                NotificationStatus.UNREAD,
                NotificationDeliveryStatus.SENT,
                "Task starts soon",
                "Focus block starts at 10:00.",
                "TASK",
                TASK_ID,
                "Reminder requested by user",
                null,
                NOW,
                null,
                null,
                null,
                null,
                null,
                0,
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
