package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdjustTimeCapitalRequest(
        @NotNull
        CapitalAdjustmentType adjustmentType,

        @NotNull
        @Positive
        Long amountInMinutes,

        @NotBlank
        @Size(max = 1000)
        String reason
) {
}
