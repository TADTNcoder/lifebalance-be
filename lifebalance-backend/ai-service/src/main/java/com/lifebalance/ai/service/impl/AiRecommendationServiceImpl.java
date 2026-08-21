package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import com.lifebalance.ai.dto.GenerateRecommendationRequest;
import com.lifebalance.ai.dto.RecommendationDecisionRequest;
import com.lifebalance.ai.error.AiExceptions;
import com.lifebalance.ai.repository.AiRecommendationRepository;
import com.lifebalance.ai.service.AiRecommendationService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AiRecommendationServiceImpl implements AiRecommendationService {

    private final AiRecommendationRepository recommendationRepository;
    private final AiSuggestionEngine suggestionEngine;
    private final AiHistoryRecorder historyRecorder;
    private final AiMapper mapper;

    AiRecommendationServiceImpl(
            AiRecommendationRepository recommendationRepository,
            AiSuggestionEngine suggestionEngine,
            AiHistoryRecorder historyRecorder,
            AiMapper mapper
    ) {
        this.recommendationRepository = recommendationRepository;
        this.suggestionEngine = suggestionEngine;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AiRecommendationResponse generate(UUID ownerId, GenerateRecommendationRequest request) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiSuggestionEngine.AiGeneratedRecommendation generatedRecommendation = suggestionEngine.recommendation(
                request.intent(),
                request.signalSummary(),
                request.targetType()
        );
        AiRecommendation recommendation = recommendationRepository.save(AiRecommendation.generate(
                ownerId,
                ownerId,
                generatedRecommendation.recommendationType(),
                generatedRecommendation.priority(),
                generatedRecommendation.title(),
                generatedRecommendation.description(),
                request.sourceType(),
                request.sourceId(),
                request.targetType(),
                request.targetId(),
                generatedRecommendation.confidenceScore(),
                request.signalSummary()
        ));
        historyRecorder.recordRecommendation(
                ownerId,
                ownerId,
                AiHistoryActionType.RECOMMENDATION_GENERATED,
                recommendation,
                null,
                mapper.recommendationSnapshot(recommendation),
                "Generated from AI recommendation request"
        );
        return mapper.toResponse(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public AiRecommendationResponse getById(UUID ownerId, UUID recommendationId) {
        AiConversationServiceImpl.validateOwner(ownerId);
        return recommendationRepository.findByIdAndOwnerId(recommendationId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> AiExceptions.recommendationNotFound(recommendationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiRecommendationResponse> search(
            UUID ownerId,
            AiRecommendationType recommendationType,
            AiRecommendationStatus status,
            AiPriority priority,
            String targetType,
            UUID targetId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiConversationServiceImpl.validateOffsetRange(from, to);
        return recommendationRepository.search(
                ownerId,
                recommendationType,
                status,
                priority,
                targetType,
                targetId,
                from,
                to,
                pageable
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public AiRecommendationResponse apply(UUID ownerId, UUID recommendationId, RecommendationDecisionRequest request) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiRecommendation recommendation = recommendationRepository.findByIdAndOwnerIdForUpdate(recommendationId, ownerId)
                .orElseThrow(() -> AiExceptions.recommendationNotFound(recommendationId));
        String oldSnapshot = mapper.recommendationSnapshot(recommendation);
        recommendation.apply(ownerId, request == null ? null : request.reason());
        historyRecorder.recordRecommendation(
                ownerId,
                ownerId,
                AiHistoryActionType.RECOMMENDATION_APPLIED,
                recommendation,
                oldSnapshot,
                mapper.recommendationSnapshot(recommendation),
                request == null ? null : request.reason()
        );
        return mapper.toResponse(recommendation);
    }

    @Override
    @Transactional
    public AiRecommendationResponse dismiss(UUID ownerId, UUID recommendationId, RecommendationDecisionRequest request) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiRecommendation recommendation = recommendationRepository.findByIdAndOwnerIdForUpdate(recommendationId, ownerId)
                .orElseThrow(() -> AiExceptions.recommendationNotFound(recommendationId));
        String oldSnapshot = mapper.recommendationSnapshot(recommendation);
        recommendation.dismiss(ownerId, request == null ? null : request.reason());
        historyRecorder.recordRecommendation(
                ownerId,
                ownerId,
                AiHistoryActionType.RECOMMENDATION_DISMISSED,
                recommendation,
                oldSnapshot,
                mapper.recommendationSnapshot(recommendation),
                request == null ? null : request.reason()
        );
        return mapper.toResponse(recommendation);
    }
}
