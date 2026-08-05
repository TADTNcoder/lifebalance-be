package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;

import java.math.BigDecimal;

public record CapitalBalanceSummaryDto(
        CapitalKind capitalType,
        BigDecimal total,
        BigDecimal allocated,
        BigDecimal available,
        BigDecimal remaining,
        BigDecimal allocatedPercentage,
        boolean overAllocated,
        String currencyCode,
        boolean initialized
) {
}
