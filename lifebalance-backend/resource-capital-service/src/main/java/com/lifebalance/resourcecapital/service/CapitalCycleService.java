package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CloseCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.ReopenCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalResponse;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CapitalCycleService {

    Page<CapitalCycleResponse> listCycles(
            UUID ownerId,
            CapitalCycleType type,
            CapitalCycleStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    Optional<CapitalCycleResponse> getActiveCycle(UUID ownerId, CapitalCycleType type);

    CapitalCycleResponse getCycle(UUID ownerId, UUID cycleId);

    CapitalCycleResponse createCycle(UUID ownerId, CreateCapitalCycleRequest request);

    CapitalCycleResponse updateCycle(UUID ownerId, UUID cycleId, UpdateCapitalCycleRequest request);

    CapitalCycleResponse activateCycle(UUID ownerId, UUID cycleId);

    CapitalCycleResponse closeCycle(UUID ownerId, UUID cycleId, CloseCapitalCycleRequest request);

    CapitalCycleResponse reopenCycle(UUID ownerId, UUID cycleId, ReopenCapitalCycleRequest request);

    TransferRemainingCapitalResponse transferRemainingCapital(
            UUID ownerId,
            UUID sourceCycleId,
            TransferRemainingCapitalRequest request
    );

    void deleteCycle(UUID ownerId, UUID cycleId);
}
