package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CapitalAdjustmentService {

    CapitalAdjustmentResponse adjustCapital(UUID ownerId, CapitalAdjustmentRequest request);

    TimeCapitalAdjustmentResponse adjustTimeCapital(UUID ownerId, UUID cycleId, AdjustTimeCapitalRequest request);

    MoneyCapitalAdjustmentResponse adjustMoneyCapital(UUID ownerId, UUID cycleId, AdjustMoneyCapitalRequest request);

    Page<CapitalAdjustmentResponse> getAdjustments(
            UUID ownerId,
            UUID capitalCycleId,
            CapitalKind capitalType,
            Pageable pageable
    );
}
