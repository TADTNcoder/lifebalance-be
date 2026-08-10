package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.CapitalSummaryResponseDTO;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;

import java.util.UUID;

public interface CapitalService {

    TimeCapitalResponse setupTimeCapital(UUID ownerId, UUID cycleId, SetupTimeCapitalRequest request);

    MoneyCapitalResponse setupMoneyCapital(UUID ownerId, UUID cycleId, SetupMoneyCapitalRequest request);

    TimeCapitalResponse getAvailableTimeCapital(UUID ownerId, UUID cycleId);

    MoneyCapitalResponse getAvailableMoneyCapital(UUID ownerId, UUID cycleId);

    TimeCapitalResponse getRemainingTimeCapital(UUID ownerId, UUID cycleId);

    MoneyCapitalResponse getRemainingMoneyCapital(UUID ownerId, UUID cycleId);

    CapitalOverviewResponse getCapitalOverview(UUID ownerId, UUID cycleId);

    CapitalSummaryResponseDTO getCapitalSummary(UUID ownerId);
}
