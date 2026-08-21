package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiConversation;
import com.lifebalance.ai.domain.AiConversationStatus;
import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.domain.AiMessage;
import com.lifebalance.ai.domain.AiMessageRole;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.domain.AiText;
import com.lifebalance.ai.dto.AiConversationResponse;
import com.lifebalance.ai.dto.AiMessageResponse;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import com.lifebalance.ai.dto.AiReplyResponse;
import com.lifebalance.ai.dto.AskAiRequest;
import com.lifebalance.ai.dto.ReasonRequest;
import com.lifebalance.ai.dto.StartConversationRequest;
import com.lifebalance.ai.error.AiExceptions;
import com.lifebalance.ai.repository.AiConversationRepository;
import com.lifebalance.ai.repository.AiMessageRepository;
import com.lifebalance.ai.repository.AiRecommendationRepository;
import com.lifebalance.ai.service.AiConversationService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AiRecommendationRepository recommendationRepository;
    private final AiSuggestionEngine suggestionEngine;
    private final AiHistoryRecorder historyRecorder;
    private final AiMapper mapper;

    AiConversationServiceImpl(
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository,
            AiRecommendationRepository recommendationRepository,
            AiSuggestionEngine suggestionEngine,
            AiHistoryRecorder historyRecorder,
            AiMapper mapper
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.recommendationRepository = recommendationRepository;
        this.suggestionEngine = suggestionEngine;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AiReplyResponse start(UUID ownerId, StartConversationRequest request) {
        validateOwner(ownerId);
        AiConversation.validateContextPair(request.contextType(), request.contextId());
        AiConversation conversation = AiConversation.start(
                ownerId,
                ownerId,
                request.title(),
                request.intent(),
                request.contextType(),
                request.contextId()
        );
        AiConversation savedConversation = conversationRepository.save(conversation);
        historyRecorder.recordConversation(
                ownerId,
                ownerId,
                AiHistoryActionType.CONVERSATION_CREATED,
                savedConversation,
                null,
                mapper.conversationSnapshot(savedConversation),
                null
        );

        String initialMessage = AiText.normalize(request.initialMessage(), AiMessage.CONTENT_MAX_LENGTH);
        if (initialMessage == null) {
            initialMessage = "Start a new LifeBalance assistant conversation.";
        }
        AiReplyResponse response = askInsideTransaction(ownerId, savedConversation, initialMessage, savedConversation.getIntent());
        return new AiReplyResponse(
                mapper.toResponse(savedConversation),
                response.userMessage(),
                response.assistantMessage(),
                response.recommendations()
        );
    }

    @Override
    @Transactional
    public AiReplyResponse ask(UUID ownerId, UUID conversationId, AskAiRequest request) {
        validateOwner(ownerId);
        AiConversation conversation = conversationRepository.findByIdAndOwnerIdForUpdate(conversationId, ownerId)
                .orElseThrow(() -> AiExceptions.conversationNotFound(conversationId));
        return askInsideTransaction(ownerId, conversation, request.message(), request.intent());
    }

    @Override
    @Transactional(readOnly = true)
    public AiConversationResponse getById(UUID ownerId, UUID conversationId) {
        validateOwner(ownerId);
        return conversationRepository.findByIdAndOwnerId(conversationId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> AiExceptions.conversationNotFound(conversationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiConversationResponse> search(
            UUID ownerId,
            AiConversationStatus status,
            AiIntent intent,
            String contextType,
            UUID contextId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        validateOwner(ownerId);
        validateOffsetRange(from, to);
        AiConversation.validateContextPair(contextType, contextId);
        return conversationRepository.search(
                ownerId,
                status,
                intent,
                AiText.normalize(contextType, AiConversation.CONTEXT_TYPE_MAX_LENGTH),
                contextId,
                from,
                to,
                pageable
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiMessageResponse> messages(UUID ownerId, UUID conversationId, Pageable pageable) {
        validateOwner(ownerId);
        if (conversationRepository.findByIdAndOwnerId(conversationId, ownerId).isEmpty()) {
            throw AiExceptions.conversationNotFound(conversationId);
        }
        return messageRepository.findConversationMessages(ownerId, conversationId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public AiConversationResponse archive(UUID ownerId, UUID conversationId, ReasonRequest request) {
        validateOwner(ownerId);
        AiConversation conversation = conversationRepository.findByIdAndOwnerIdForUpdate(conversationId, ownerId)
                .orElseThrow(() -> AiExceptions.conversationNotFound(conversationId));
        String oldSnapshot = mapper.conversationSnapshot(conversation);
        conversation.archive(ownerId);
        historyRecorder.recordConversation(
                ownerId,
                ownerId,
                AiHistoryActionType.CONVERSATION_ARCHIVED,
                conversation,
                oldSnapshot,
                mapper.conversationSnapshot(conversation),
                request == null ? null : request.reason()
        );
        return mapper.toResponse(conversation);
    }

    private AiReplyResponse askInsideTransaction(
            UUID ownerId,
            AiConversation conversation,
            String message,
            AiIntent requestedIntent
    ) {
        conversation.touch(ownerId);
        AiIntent resolvedIntent = requestedIntent == null ? conversation.getIntent() : requestedIntent;
        AiMessage userMessage = messageRepository.save(AiMessage.record(
                conversation,
                ownerId,
                ownerId,
                AiMessageRole.USER,
                resolvedIntent,
                message,
                null
        ));
        historyRecorder.recordMessage(
                ownerId,
                ownerId,
                AiHistoryActionType.MESSAGE_RECORDED,
                conversation,
                userMessage,
                null,
                mapper.messageSnapshot(userMessage),
                null
        );

        AiSuggestionEngine.AiGeneratedReply generatedReply = suggestionEngine.reply(resolvedIntent, message);
        AiMessage assistantMessage = messageRepository.save(AiMessage.record(
                conversation,
                ownerId,
                ownerId,
                AiMessageRole.ASSISTANT,
                generatedReply.intent(),
                generatedReply.content(),
                generatedReply.modelName()
        ));
        historyRecorder.recordMessage(
                ownerId,
                ownerId,
                AiHistoryActionType.ASSISTANT_RESPONDED,
                conversation,
                assistantMessage,
                null,
                mapper.messageSnapshot(assistantMessage),
                generatedReply.modelName()
        );

        AiRecommendation recommendation = recommendationRepository.save(AiRecommendation.generate(
                ownerId,
                ownerId,
                generatedReply.recommendationType(),
                generatedReply.priority(),
                "AI assistant follow-up",
                generatedReply.content(),
                conversation.getContextType(),
                conversation.getContextId(),
                conversation.getContextType(),
                conversation.getContextId(),
                generatedReply.confidenceScore(),
                message
        ));
        historyRecorder.recordRecommendation(
                ownerId,
                ownerId,
                AiHistoryActionType.RECOMMENDATION_GENERATED,
                recommendation,
                null,
                mapper.recommendationSnapshot(recommendation),
                "Generated from AI conversation"
        );

        List<AiRecommendationResponse> recommendations = List.of(mapper.toResponse(recommendation));
        return new AiReplyResponse(
                mapper.toResponse(conversation),
                mapper.toResponse(userMessage),
                mapper.toResponse(assistantMessage),
                recommendations
        );
    }

    static void validateOwner(UUID ownerId) {
        if (ownerId == null) {
            throw AiExceptions.invalidRequest("ownerId is required.");
        }
    }

    static void validateOffsetRange(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw AiExceptions.invalidPeriod("from must be before or equal to to.");
        }
    }
}
