package com.lifebalance.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFinanceCategoryRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 20) String color,
        @Size(max = 50) String icon,
        @Size(max = 1000) String reason
) {
}
