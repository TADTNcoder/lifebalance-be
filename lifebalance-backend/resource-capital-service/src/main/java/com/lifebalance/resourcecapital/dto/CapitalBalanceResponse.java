package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;

import java.util.UUID;

public record CapitalBalanceResponse(
        UUID cycleId,
        CapitalCycleStatus cycleStatus,
        CapitalBalanceSummaryDto timeCapital,
        CapitalBalanceSummaryDto moneyCapital
) {
}
