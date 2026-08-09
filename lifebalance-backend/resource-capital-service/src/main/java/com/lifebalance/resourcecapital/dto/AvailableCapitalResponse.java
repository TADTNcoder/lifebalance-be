package com.lifebalance.resourcecapital.dto;

import java.util.UUID;

public record AvailableCapitalResponse(
        UUID cycleId,
        AvailableTimeCapitalResponse timeCapital,
        AvailableMoneyCapitalResponse moneyCapital
) {
}
