package com.lifebalance.analytics.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Optional planning snapshot carried with an actual-record mutation so the
 * analytics service can evaluate the task without a second user action.
 */
public record AutomaticEvaluationBaselineRequest(
        LocalDate periodStart,
        LocalDate periodEnd,
        @PositiveOrZero Integer plannedMinutes,
        @DecimalMin(value = "0.0000") BigDecimal plannedCost,
        @Size(min = 3, max = 3) String currencyCode
) {
}
