package com.lifebalance.resourcecapital.dto;

import java.util.UUID;

public record RemainingCapitalResponse(
        UUID cycleId,
        RemainingTimeCapitalResponse timeCapital,
        RemainingMoneyCapitalResponse moneyCapital
) {
}
