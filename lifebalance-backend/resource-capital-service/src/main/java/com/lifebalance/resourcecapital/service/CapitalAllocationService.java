package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequestDTO;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.AllocationResponseDTO;
import com.lifebalance.resourcecapital.dto.CapitalAllocationChangeRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalReallocationRequest;
import com.lifebalance.resourcecapital.dto.ChangeCapitalAllocationRequestDTO;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import com.lifebalance.resourcecapital.dto.OverAllocationConfirmationResponse;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequestDTO;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CapitalAllocationService {

    AllocationResponse allocateCapital(UUID ownerId, CreateCapitalAllocationRequest request);

    OverAllocationConfirmationResponse prepareOverAllocationConfirmation(
            UUID ownerId,
            CreateCapitalAllocationRequest request
    );

    AllocationResponseDTO allocateCapital(UUID ownerId, AllocateCapitalRequestDTO request);

    AllocationResponse reallocateCapital(UUID ownerId, CapitalReallocationRequest request);

    AllocationResponseDTO reallocateCapital(UUID ownerId, ReallocateCapitalRequestDTO request);

    AllocationResponse changeAllocation(UUID ownerId, UUID allocationId, CapitalAllocationChangeRequest request);

    AllocationResponseDTO changeAllocation(UUID ownerId, ChangeCapitalAllocationRequestDTO request);

    AllocationResponse releaseCapital(UUID ownerId, UUID allocationId, CapitalAllocationReleaseRequest request);

    AllocationResponseDTO releaseCapital(UUID ownerId, ReleaseCapitalRequestDTO request);

    List<AllocationResponseDTO> getAllocationsByCycle(UUID ownerId, UUID cycleId, CapitalKind capitalType);

    Page<CapitalAllocationResponse> getAllocations(
            UUID ownerId,
            UUID capitalCycleId,
            UUID taskId,
            CapitalKind capitalType,
            AllocationStatus status,
            Pageable pageable
    );
}
