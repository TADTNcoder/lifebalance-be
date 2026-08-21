package com.lifebalance.ai.dto;

import jakarta.validation.constraints.Size;

public record ReasonRequest(
        @Size(max = 1000) String reason
) {
}
