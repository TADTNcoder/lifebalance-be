package com.lifebalance.finance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetStatusResponse(
        BudgetResponse budget,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal usagePercent,
        boolean thresholdReached,
        UUID historyReferenceId
) {
}
