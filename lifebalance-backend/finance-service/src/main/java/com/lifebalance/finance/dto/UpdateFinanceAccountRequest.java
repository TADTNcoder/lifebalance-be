package com.lifebalance.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFinanceAccountRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String reason
) {
}
