package com.lifebalance.ai.dto;

import java.util.List;

public record AiReplyResponse(
        AiConversationResponse conversation,
        AiMessageResponse userMessage,
        AiMessageResponse assistantMessage,
        List<AiRecommendationResponse> recommendations
) {
}
