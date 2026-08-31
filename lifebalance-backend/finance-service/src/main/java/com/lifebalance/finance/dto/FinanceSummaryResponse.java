package com.lifebalance.finance.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FinanceSummaryResponse(
        UUID ownerId,
        String currencyCode,
        OffsetDateTime fromDate,
        OffsetDateTime toDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netCashflow,
        BigDecimal mainPoolBalance,
        BigDecimal totalJarBalance,
        BigDecimal totalAccountBalance,
        BigDecimal openingAccountBalance,
        List<FinanceMonthlyJarSettlementSummaryResponse> monthlyJarSettlements
) {
}
