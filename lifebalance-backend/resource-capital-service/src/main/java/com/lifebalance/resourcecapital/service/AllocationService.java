package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequest;

import java.util.UUID;

public interface AllocationService {

    AllocationResponse allocateCapital(UUID ownerId, UUID cycleId, AllocateCapitalRequest request);

    AllocationResponse reallocateCapital(UUID ownerId, UUID cycleId, ReallocateCapitalRequest request);

    AllocationResponse releaseCapital(UUID ownerId, UUID cycleId, ReleaseCapitalRequest request);
}
