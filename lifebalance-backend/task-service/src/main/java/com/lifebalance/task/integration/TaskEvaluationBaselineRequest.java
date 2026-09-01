package com.lifebalance.task.integration;

import java.math.BigDecimal;
import java.time.LocalDate;

record TaskEvaluationBaselineRequest(
        LocalDate periodStart,
        LocalDate periodEnd,
        Integer plannedMinutes,
        BigDecimal plannedCost,
        String currencyCode
) {
}
