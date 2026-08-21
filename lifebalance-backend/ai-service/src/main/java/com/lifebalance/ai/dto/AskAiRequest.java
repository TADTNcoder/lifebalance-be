package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiIntent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskAiRequest(
        @NotBlank @Size(max = 4000) String message,
        AiIntent intent
) {
}
