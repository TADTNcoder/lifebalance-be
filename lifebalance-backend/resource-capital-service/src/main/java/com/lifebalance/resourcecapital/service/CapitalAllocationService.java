package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalReallocationRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CapitalAllocationService {

    AllocationResponse allocateCapital(UUID ownerId, CreateCapitalAllocationRequest request);

    AllocationResponse reallocateCapital(UUID ownerId, CapitalReallocationRequest request);

    AllocationResponse releaseCapital(UUID ownerId, UUID allocationId, CapitalAllocationReleaseRequest request);

    Page<CapitalAllocationResponse> getAllocations(
            UUID ownerId,
            UUID capitalCycleId,
            UUID taskId,
            CapitalKind capitalType,
            AllocationStatus status,
            Pageable pageable
    );
}
