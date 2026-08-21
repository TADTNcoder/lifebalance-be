package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiConversation;
import com.lifebalance.ai.domain.AiHistory;
import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiInsight;
import com.lifebalance.ai.domain.AiMessage;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.repository.AiHistoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AiHistoryRecorder {

    private final AiHistoryRepository historyRepository;

    AiHistoryRecorder(AiHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    void recordConversation(
            UUID ownerId,
            UUID actorId,
            AiHistoryActionType actionType,
            AiConversation conversation,
            String oldValue,
            String newValue,
            String reason
    ) {
        save(ownerId, actorId, actionType, conversation, null, null, null, oldValue, newValue, reason);
    }

    void recordMessage(
            UUID ownerId,
            UUID actorId,
            AiHistoryActionType actionType,
            AiConversation conversation,
            AiMessage message,
            String oldValue,
            String newValue,
            String reason
    ) {
        save(ownerId, actorId, actionType, conversation, message, null, null, oldValue, newValue, reason);
    }

    void recordRecommendation(
            UUID ownerId,
            UUID actorId,
            AiHistoryActionType actionType,
            AiRecommendation recommendation,
            String oldValue,
            String newValue,
            String reason
    ) {
        save(ownerId, actorId, actionType, null, null, recommendation, null, oldValue, newValue, reason);
    }

    void recordInsight(
            UUID ownerId,
            UUID actorId,
            AiHistoryActionType actionType,
            AiInsight insight,
            String oldValue,
            String newValue,
            String reason
    ) {
        save(ownerId, actorId, actionType, null, null, null, insight, oldValue, newValue, reason);
    }

    private void save(
            UUID ownerId,
            UUID actorId,
            AiHistoryActionType actionType,
            AiConversation conversation,
            AiMessage message,
            AiRecommendation recommendation,
            AiInsight insight,
            String oldValue,
            String newValue,
            String reason
    ) {
        historyRepository.save(AiHistory.record(
                ownerId,
                actorId,
                actionType,
                conversation,
                message,
                recommendation,
                insight,
                oldValue,
                newValue,
                reason
        ));
    }
}
