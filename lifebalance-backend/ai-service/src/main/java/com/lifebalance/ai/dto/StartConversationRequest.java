package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiIntent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StartConversationRequest(
        @NotBlank @Size(max = 200) String title,
        AiIntent intent,
        @Size(max = 64) String contextType,
        UUID contextId,
        @Size(max = 4000) String initialMessage
) {
}
