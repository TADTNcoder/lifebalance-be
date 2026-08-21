package com.lifebalance.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReasonRequest(
        @NotBlank @Size(max = 1000) String reason
) {
}
