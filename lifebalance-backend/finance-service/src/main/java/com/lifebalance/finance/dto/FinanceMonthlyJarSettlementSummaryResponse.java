package com.lifebalance.finance.dto;

import java.math.BigDecimal;

public record FinanceMonthlyJarSettlementSummaryResponse(
        String period,
        BigDecimal allocatedAmount,
        BigDecimal actualExpenseAmount,
        BigDecimal closingBalance,
        BigDecimal returnedAmount,
        BigDecimal coveredDeficitAmount,
        BigDecimal varianceAmount,
        int settledJarCount
) {
}
