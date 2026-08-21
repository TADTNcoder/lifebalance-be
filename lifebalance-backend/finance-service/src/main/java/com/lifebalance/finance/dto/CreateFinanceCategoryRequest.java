package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceCategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFinanceCategoryRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull FinanceCategoryType categoryType,
        @Size(max = 20) String color,
        @Size(max = 50) String icon
) {
}
