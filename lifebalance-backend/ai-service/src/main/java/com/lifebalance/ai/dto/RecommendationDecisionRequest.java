package com.lifebalance.ai.dto;

import jakarta.validation.constraints.Size;

public record RecommendationDecisionRequest(
        @Size(max = 1000) String reason
) {
}
