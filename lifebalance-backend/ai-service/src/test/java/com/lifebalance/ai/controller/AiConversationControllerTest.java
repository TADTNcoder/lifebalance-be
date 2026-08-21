package com.lifebalance.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.ai.domain.AiConversationStatus;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.domain.AiMessageRole;
import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
import com.lifebalance.ai.dto.AiConversationResponse;
import com.lifebalance.ai.dto.AiMessageResponse;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import com.lifebalance.ai.dto.AiReplyResponse;
import com.lifebalance.ai.dto.StartConversationRequest;
import com.lifebalance.ai.service.AiConversationService;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
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

@WebMvcTest(AiConversationController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        AiConversationControllerTest.TestSecuritySupport.class
})
class AiConversationControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CONVERSATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID USER_MESSAGE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID ASSISTANT_MESSAGE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID RECOMMENDATION_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-21T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiConversationService conversationService;

    @Test
    void startReturnsCreatedAndDelegatesAuthenticatedOwner() throws Exception {
        when(conversationService.start(eq(OWNER_ID), any(StartConversationRequest.class))).thenReturn(reply());

        mockMvc.perform(post("/api/ai/conversations")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Plan sprint timeline",
                                  "intent": "TIMELINE_OPTIMIZATION",
                                  "contextType": "TASK",
                                  "contextId": "%s",
                                  "initialMessage": "There is a timeline conflict."
                                }
                                """.formatted(TASK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conversation.id").value(CONVERSATION_ID.toString()))
                .andExpect(jsonPath("$.data.assistantMessage.role").value("ASSISTANT"))
                .andExpect(jsonPath("$.data.recommendations[0].recommendationType").value("SCHEDULE_ADJUSTMENT"));

        verify(conversationService).start(eq(OWNER_ID), any(StartConversationRequest.class));
    }

    @Test
    void startReturnsBadRequestWhenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/ai/conversations")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intent": "GENERAL",
                                  "initialMessage": "Hello"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.title").exists());

        verify(conversationService, never()).start(any(), any());
    }

    @Test
    void startReturnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        mockMvc.perform(post("/api/ai/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Plan sprint timeline",
                                  "intent": "GENERAL"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(conversationService, never()).start(any(), any());
    }

    private static AiReplyResponse reply() {
        AiConversationResponse conversation = new AiConversationResponse(
                CONVERSATION_ID,
                OWNER_ID,
                OWNER_ID,
                "Plan sprint timeline",
                AiIntent.TIMELINE_OPTIMIZATION,
                "TASK",
                TASK_ID,
                AiConversationStatus.ACTIVE,
                null,
                OWNER_ID,
                OWNER_ID,
                NOW,
                NOW
        );
        AiMessageResponse userMessage = new AiMessageResponse(
                USER_MESSAGE_ID,
                CONVERSATION_ID,
                OWNER_ID,
                OWNER_ID,
                AiMessageRole.USER,
                AiIntent.TIMELINE_OPTIMIZATION,
                "There is a timeline conflict.",
                null,
                8,
                NOW
        );
        AiMessageResponse assistantMessage = new AiMessageResponse(
                ASSISTANT_MESSAGE_ID,
                CONVERSATION_ID,
                OWNER_ID,
                OWNER_ID,
                AiMessageRole.ASSISTANT,
                AiIntent.TIMELINE_OPTIMIZATION,
                "Protect the most constrained time block first.",
                "lifebalance-rule-engine-v1",
                10,
                NOW
        );
        AiRecommendationResponse recommendation = new AiRecommendationResponse(
                RECOMMENDATION_ID,
                OWNER_ID,
                OWNER_ID,
                AiRecommendationType.SCHEDULE_ADJUSTMENT,
                AiRecommendationStatus.PENDING,
                AiPriority.HIGH,
                "Adjust schedule pressure",
                "Review the current schedule signal.",
                "TASK",
                TASK_ID,
                "TASK",
                TASK_ID,
                new BigDecimal("0.7800"),
                "timeline conflict",
                NOW,
                null,
                null,
                OWNER_ID,
                OWNER_ID,
                NOW,
                NOW
        );
        return new AiReplyResponse(conversation, userMessage, assistantMessage, List.of(recommendation));
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
