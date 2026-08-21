package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiIntent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GenerateRecommendationRequest(
        AiIntent intent,
        @Size(max = 64) String sourceType,
        UUID sourceId,
        @Size(max = 64) String targetType,
        UUID targetId,
        @NotBlank @Size(max = 2000) String signalSummary
) {
}
