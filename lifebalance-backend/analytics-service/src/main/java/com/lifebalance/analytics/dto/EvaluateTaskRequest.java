package com.lifebalance.analytics.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EvaluateTaskRequest(
        @NotNull UUID taskId,
        UUID capitalCycleId,
        LocalDate periodStart,
        LocalDate periodEnd,
        @PositiveOrZero Integer plannedMinutes,
        @PositiveOrZero Integer actualMinutes,
        @DecimalMin(value = "0.0000") BigDecimal plannedCost,
        @DecimalMin(value = "0.0000") BigDecimal actualCost,
        @Size(min = 3, max = 3) String currencyCode,
        @Size(max = 1000) String reason
) {
}
