package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.ResourceBreakdownDto;

import java.util.List;
import java.util.UUID;

public interface CapitalBalanceService {

    CapitalBalanceResponse getCycleBalance(UUID ownerId, UUID cycleId);

    List<ResourceBreakdownDto> getAllocationBreakdownByTarget(
            UUID ownerId,
            UUID cycleId,
            AllocationTargetType targetType
    );
}
