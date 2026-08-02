package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;

import java.util.UUID;

public record CapitalOverviewResponse(
        UUID cycleId,
        CapitalCycleStatus cycleStatus,
        TimeCapitalResponse timeCapital,
        MoneyCapitalResponse moneyCapital
) {
}
