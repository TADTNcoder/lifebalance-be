package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;

import java.util.UUID;

public interface CapitalAdjustmentService {

    TimeCapitalAdjustmentResponse adjustTimeCapital(UUID ownerId, UUID cycleId, AdjustTimeCapitalRequest request);

    MoneyCapitalAdjustmentResponse adjustMoneyCapital(UUID ownerId, UUID cycleId, AdjustMoneyCapitalRequest request);
}
