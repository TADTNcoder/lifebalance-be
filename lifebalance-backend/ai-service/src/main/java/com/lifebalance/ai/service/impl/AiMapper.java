package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiConversation;
import com.lifebalance.ai.domain.AiHistory;
import com.lifebalance.ai.domain.AiInsight;
import com.lifebalance.ai.domain.AiMessage;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.dto.AiConversationResponse;
import com.lifebalance.ai.dto.AiHistoryResponse;
import com.lifebalance.ai.dto.AiInsightResponse;
import com.lifebalance.ai.dto.AiMessageResponse;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import org.springframework.stereotype.Component;

@Component
class AiMapper {

    AiConversationResponse toResponse(AiConversation conversation) {
        return new AiConversationResponse(
                conversation.getId(),
                conversation.getOwnerId(),
                conversation.getActorId(),
                conversation.getTitle(),
                conversation.getIntent(),
                conversation.getContextType(),
                conversation.getContextId(),
                conversation.getStatus(),
                conversation.getArchivedAt(),
                conversation.getCreatedBy(),
                conversation.getUpdatedBy(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    AiMessageResponse toResponse(AiMessage message) {
        return new AiMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getOwnerId(),
                message.getActorId(),
                message.getRole(),
                message.getIntent(),
                message.getContent(),
                message.getModelName(),
                message.getTokenEstimate(),
                message.getCreatedAt()
        );
    }

    AiRecommendationResponse toResponse(AiRecommendation recommendation) {
        return new AiRecommendationResponse(
                recommendation.getId(),
                recommendation.getOwnerId(),
                recommendation.getActorId(),
                recommendation.getRecommendationType(),
                recommendation.getStatus(),
                recommendation.getPriority(),
                recommendation.getTitle(),
                recommendation.getDescription(),
                recommendation.getSourceType(),
                recommendation.getSourceId(),
                recommendation.getTargetType(),
                recommendation.getTargetId(),
                recommendation.getConfidenceScore(),
                recommendation.getSignalSummary(),
                recommendation.getGeneratedAt(),
                recommendation.getDecidedAt(),
                recommendation.getDecisionReason(),
                recommendation.getCreatedBy(),
                recommendation.getUpdatedBy(),
                recommendation.getCreatedAt(),
                recommendation.getUpdatedAt()
        );
    }

    AiInsightResponse toResponse(AiInsight insight) {
        return new AiInsightResponse(
                insight.getId(),
                insight.getOwnerId(),
                insight.getActorId(),
                insight.getInsightType(),
                insight.getSeverity(),
                insight.getStatus(),
                insight.getTitle(),
                insight.getSummary(),
                insight.getPeriodStart(),
                insight.getPeriodEnd(),
                insight.getReferenceType(),
                insight.getReferenceId(),
                insight.getConfidenceScore(),
                insight.getSignalSummary(),
                insight.getGeneratedAt(),
                insight.getArchivedAt(),
                insight.getCreatedBy(),
                insight.getUpdatedBy(),
                insight.getCreatedAt(),
                insight.getUpdatedAt()
        );
    }

    AiHistoryResponse toResponse(AiHistory history) {
        return new AiHistoryResponse(
                history.getId(),
                history.getOwnerId(),
                history.getActorId(),
                history.getActionType(),
                history.getConversation() == null ? null : history.getConversation().getId(),
                history.getMessage() == null ? null : history.getMessage().getId(),
                history.getRecommendation() == null ? null : history.getRecommendation().getId(),
                history.getInsight() == null ? null : history.getInsight().getId(),
                history.getOldValue(),
                history.getNewValue(),
                history.getReason(),
                history.getOccurredAt()
        );
    }

    String conversationSnapshot(AiConversation conversation) {
        return "id=%s;title=%s;intent=%s;status=%s;context=%s:%s"
                .formatted(
                        conversation.getId(),
                        conversation.getTitle(),
                        conversation.getIntent(),
                        conversation.getStatus(),
                        conversation.getContextType(),
                        conversation.getContextId()
                );
    }

    String messageSnapshot(AiMessage message) {
        return "id=%s;conversationId=%s;role=%s;intent=%s;tokenEstimate=%s"
                .formatted(
                        message.getId(),
                        message.getConversation().getId(),
                        message.getRole(),
                        message.getIntent(),
                        message.getTokenEstimate()
                );
    }

    String recommendationSnapshot(AiRecommendation recommendation) {
        return "id=%s;type=%s;status=%s;priority=%s;confidence=%s;target=%s:%s"
                .formatted(
                        recommendation.getId(),
                        recommendation.getRecommendationType(),
                        recommendation.getStatus(),
                        recommendation.getPriority(),
                        recommendation.getConfidenceScore(),
                        recommendation.getTargetType(),
                        recommendation.getTargetId()
                );
    }

    String insightSnapshot(AiInsight insight) {
        return "id=%s;type=%s;severity=%s;status=%s;confidence=%s;reference=%s:%s"
                .formatted(
                        insight.getId(),
                        insight.getInsightType(),
                        insight.getSeverity(),
                        insight.getStatus(),
                        insight.getConfidenceScore(),
                        insight.getReferenceType(),
                        insight.getReferenceId()
                );
    }
}
