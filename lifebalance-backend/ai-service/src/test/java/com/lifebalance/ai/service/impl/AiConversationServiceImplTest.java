package com.lifebalance.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lifebalance.ai.domain.AiConversation;
import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.domain.AiMessage;
import com.lifebalance.ai.domain.AiMessageRole;
import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.domain.AiRecommendationType;
import com.lifebalance.ai.dto.AiReplyResponse;
import com.lifebalance.ai.dto.AskAiRequest;
import com.lifebalance.ai.error.AiErrorCode;
import com.lifebalance.ai.repository.AiConversationRepository;
import com.lifebalance.ai.repository.AiMessageRepository;
import com.lifebalance.ai.repository.AiRecommendationRepository;
import com.lifebalance.common.error.AppException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiConversationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CONVERSATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID USER_MESSAGE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID ASSISTANT_MESSAGE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID RECOMMENDATION_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Mock
    private AiConversationRepository conversationRepository;

    @Mock
    private AiMessageRepository messageRepository;

    @Mock
    private AiRecommendationRepository recommendationRepository;

    @Mock
    private AiHistoryRecorder historyRecorder;

    @Test
    void askStoresUserMessageAssistantReplyAndRecommendation() {
        AiConversation conversation = conversation();
        when(conversationRepository.findByIdAndOwnerIdForUpdate(CONVERSATION_ID, OWNER_ID))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AiMessage.class))).thenAnswer(invocation -> {
            AiMessage message = invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    message,
                    "id",
                    message.getRole() == AiMessageRole.USER ? USER_MESSAGE_ID : ASSISTANT_MESSAGE_ID
            );
            return message;
        });
        when(recommendationRepository.save(any(AiRecommendation.class))).thenAnswer(invocation -> {
            AiRecommendation recommendation = invocation.getArgument(0);
            ReflectionTestUtils.setField(recommendation, "id", RECOMMENDATION_ID);
            return recommendation;
        });

        AiReplyResponse response = service().ask(
                OWNER_ID,
                CONVERSATION_ID,
                new AskAiRequest("There is a timeline conflict and capital shortage.", AiIntent.TIMELINE_OPTIMIZATION)
        );

        assertThat(response.conversation().id()).isEqualTo(CONVERSATION_ID);
        assertThat(response.userMessage().id()).isEqualTo(USER_MESSAGE_ID);
        assertThat(response.userMessage().role()).isEqualTo(AiMessageRole.USER);
        assertThat(response.assistantMessage().id()).isEqualTo(ASSISTANT_MESSAGE_ID);
        assertThat(response.assistantMessage().content()).contains("Protect", "validate constraints");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).id()).isEqualTo(RECOMMENDATION_ID);
        assertThat(response.recommendations().get(0).recommendationType())
                .isEqualTo(AiRecommendationType.CAPITAL_ALLOCATION);
        assertThat(response.recommendations().get(0).priority()).isEqualTo(AiPriority.HIGH);

        verify(historyRecorder).recordMessage(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AiHistoryActionType.MESSAGE_RECORDED),
                eq(conversation),
                any(AiMessage.class),
                isNull(),
                contains("role=USER"),
                isNull()
        );
        verify(historyRecorder).recordMessage(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AiHistoryActionType.ASSISTANT_RESPONDED),
                eq(conversation),
                any(AiMessage.class),
                isNull(),
                contains("role=ASSISTANT"),
                eq("lifebalance-rule-engine-v1")
        );
        verify(historyRecorder).recordRecommendation(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AiHistoryActionType.RECOMMENDATION_GENERATED),
                any(AiRecommendation.class),
                isNull(),
                contains("priority=HIGH"),
                eq("Generated from AI conversation")
        );
    }

    @Test
    void askRejectsMissingConversationWithoutChangingRecords() {
        when(conversationRepository.findByIdAndOwnerIdForUpdate(CONVERSATION_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().ask(
                OWNER_ID,
                CONVERSATION_ID,
                new AskAiRequest("Can you help?", AiIntent.GENERAL)
        ))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(AiErrorCode.CONVERSATION_NOT_FOUND);

        verify(messageRepository, never()).save(any());
        verify(recommendationRepository, never()).save(any());
        verifyNoInteractions(historyRecorder);
    }

    private AiConversationServiceImpl service() {
        return new AiConversationServiceImpl(
                conversationRepository,
                messageRepository,
                recommendationRepository,
                new AiSuggestionEngine(),
                historyRecorder,
                new AiMapper()
        );
    }

    private static AiConversation conversation() {
        AiConversation conversation = AiConversation.start(
                OWNER_ID,
                OWNER_ID,
                "Plan sprint timeline",
                AiIntent.TIMELINE_OPTIMIZATION,
                "TASK",
                TASK_ID
        );
        ReflectionTestUtils.setField(conversation, "id", CONVERSATION_ID);
        return conversation;
    }
}
