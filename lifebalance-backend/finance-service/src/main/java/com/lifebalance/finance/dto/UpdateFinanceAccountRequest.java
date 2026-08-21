package com.lifebalance.finance.dto;

import com.lifebalance.finance.domain.FinanceAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFinanceAccountRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull FinanceAccountType accountType,
        @Size(max = 1000) String reason
) {
}
