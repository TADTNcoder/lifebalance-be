package com.lifebalance.resourcecapital.dto;

import java.math.BigDecimal;

public record AvailableMoneyCapitalResponse(
        BigDecimal availableAmount,
        String currencyCode,
        boolean initialized
) {
}
