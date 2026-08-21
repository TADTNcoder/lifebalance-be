package com.lifebalance.ai.service;

import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import com.lifebalance.ai.dto.GenerateRecommendationRequest;
import com.lifebalance.ai.dto.RecommendationDecisionRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiRecommendationService {

    AiRecommendationResponse generate(UUID ownerId, GenerateRecommendationRequest request);

    AiRecommendationResponse getById(UUID ownerId, UUID recommendationId);

    Page<AiRecommendationResponse> search(
            UUID ownerId,
            AiRecommendationType recommendationType,
            AiRecommendationStatus status,
            AiPriority priority,
            String targetType,
            UUID targetId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    AiRecommendationResponse apply(UUID ownerId, UUID recommendationId, RecommendationDecisionRequest request);

    AiRecommendationResponse dismiss(UUID ownerId, UUID recommendationId, RecommendationDecisionRequest request);
}
