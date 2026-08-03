package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleOverlapException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CloseCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.ReopenCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.service.CapitalCycleBusinessValidator;
import com.lifebalance.resourcecapital.service.CapitalCycleService;
import com.lifebalance.resourcecapital.service.mapper.CapitalCycleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalCycleServiceImpl implements CapitalCycleService {

    private static final String ACTIVE_CYCLE_UNIQUE_INDEX = "uq_capital_cycles_owner_type_active";

    private final CapitalCycleRepository capitalCycleRepository;
    private final CapitalCycleMapper capitalCycleMapper;
    private final CapitalCycleBusinessValidator capitalCycleBusinessValidator;
    private final Clock clock;

    @Autowired
    public CapitalCycleServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            CapitalCycleMapper capitalCycleMapper,
            CapitalCycleBusinessValidator capitalCycleBusinessValidator
    ) {
        this(capitalCycleRepository, capitalCycleMapper, capitalCycleBusinessValidator, Clock.systemUTC());
    }

    CapitalCycleServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            CapitalCycleMapper capitalCycleMapper,
            CapitalCycleBusinessValidator capitalCycleBusinessValidator,
            Clock clock
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.capitalCycleMapper = capitalCycleMapper;
        this.capitalCycleBusinessValidator = capitalCycleBusinessValidator;
        this.clock = clock;
    }

    @Transactional
    @Override
    public CapitalCycleResponse createCycle(UUID ownerId, CreateCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Create capital cycle request is required.");
        CapitalCycle cycle = CapitalCycle.create(
                ownerId,
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getStartDate(),
                request.getEndDate()
        );
        ensureNoOverlappingCycle(
                ownerId,
                request.getType(),
                request.getStartDate(),
                request.getEndDate(),
                null
        );

        return capitalCycleMapper.toResponse(capitalCycleRepository.save(cycle));
    }

    @Transactional
    @Override
    public CapitalCycleResponse updateCycle(UUID ownerId, UUID cycleId, UpdateCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Update capital cycle request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);

        if (!cycle.isActive()) {
            ensureNoOverlappingCycle(
                    ownerId,
                    request.getType(),
                    request.getStartDate(),
                    request.getEndDate(),
                    cycleId
            );
        }

        cycle.updateInformation(
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getStartDate(),
                request.getEndDate()
        );

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public CapitalCycleResponse activateCycle(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        ensureTransitionAllowed(cycle, CapitalCycleStatus.ACTIVE, "activate");
        capitalCycleBusinessValidator.validateActivationAllowed(ownerId, cycle.getType(), cycle.getId());

        try {
            cycle.activate(clock.instant());
            capitalCycleRepository.saveAndFlush(cycle);
        } catch (DataIntegrityViolationException exception) {
            if (isActiveCycleUniqueViolation(exception)) {
                throw new ActiveCapitalCycleAlreadyExistsException(ownerId, cycle.getType());
            }
            throw exception;
        }

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public CapitalCycleResponse closeCycle(UUID ownerId, UUID cycleId, CloseCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Close capital cycle request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);

        cycle.close(request.getReason(), clock.instant());

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public CapitalCycleResponse reopenCycle(UUID ownerId, UUID cycleId, ReopenCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Reopen capital cycle request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);

        cycle.reopen(request.getReason(), clock.instant());

        return capitalCycleMapper.toResponse(cycle);
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId, ownerId));
    }

    private void ensureNoOverlappingCycle(
            UUID ownerId,
            CapitalCycleType type,
            LocalDate startDate,
            LocalDate endDate,
            UUID excludedCycleId
    ) {
        if (capitalCycleRepository.existsOverlappingCycle(ownerId, type, startDate, endDate, excludedCycleId)) {
            throw new CapitalCycleOverlapException(ownerId, startDate, endDate);
        }
    }

    private void ensureTransitionAllowed(CapitalCycle cycle, CapitalCycleStatus targetStatus, String action) {
        if (!cycle.getStatus().canTransitionTo(targetStatus)) {
            throw new InvalidCapitalCycleStateException(cycle.getId(), cycle.getStatus(), targetStatus, action);
        }
    }

    private boolean isActiveCycleUniqueViolation(DataIntegrityViolationException exception) {
        Throwable mostSpecificCause = exception.getMostSpecificCause();
        String message = mostSpecificCause.getMessage();
        return message != null && message.contains(ACTIVE_CYCLE_UNIQUE_INDEX);
    }

}
