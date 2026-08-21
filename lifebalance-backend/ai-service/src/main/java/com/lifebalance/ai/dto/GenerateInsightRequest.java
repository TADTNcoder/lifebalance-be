package com.lifebalance.ai.dto;

import com.lifebalance.ai.domain.AiInsightType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record GenerateInsightRequest(
        AiInsightType insightType,
        LocalDate periodStart,
        LocalDate periodEnd,
        @Size(max = 64) String referenceType,
        UUID referenceId,
        @NotBlank @Size(max = 2000) String signalSummary
) {
}
