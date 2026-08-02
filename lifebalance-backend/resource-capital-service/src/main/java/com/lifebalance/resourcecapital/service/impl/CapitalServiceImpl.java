package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.resourcecapital.service.mapper.CapitalMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalServiceImpl implements CapitalService {

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalMapper capitalMapper;

    public CapitalServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalMapper capitalMapper
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalMapper = capitalMapper;
    }

    @Transactional
    @Override
    public TimeCapitalResponse setupTimeCapital(UUID ownerId, UUID cycleId, SetupTimeCapitalRequest request) {
        Objects.requireNonNull(request, "Setup time capital request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        ensureTimeCapitalNotInitialized(cycleId);

        TimeCapital timeCapital = TimeCapital.create(cycle, request.plannedMinutes());
        try {
            return capitalMapper.toTimeResponse(timeCapitalRepository.saveAndFlush(timeCapital));
        } catch (DataIntegrityViolationException exception) {
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.TIME, exception);
        }
    }

    @Transactional
    @Override
    public MoneyCapitalResponse setupMoneyCapital(UUID ownerId, UUID cycleId, SetupMoneyCapitalRequest request) {
        Objects.requireNonNull(request, "Setup money capital request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        ensureMoneyCapitalNotInitialized(cycleId);

        MoneyCapital moneyCapital = MoneyCapital.create(cycle, request.plannedAmount(), request.currencyCode());
        try {
            return capitalMapper.toMoneyResponse(moneyCapitalRepository.saveAndFlush(moneyCapital));
        } catch (DataIntegrityViolationException exception) {
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.MONEY, exception);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TimeCapitalResponse getAvailableTimeCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toTimeResponse(findTimeCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public MoneyCapitalResponse getAvailableMoneyCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toMoneyResponse(findMoneyCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public TimeCapitalResponse getRemainingTimeCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toTimeResponse(findTimeCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public MoneyCapitalResponse getRemainingMoneyCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toMoneyResponse(findMoneyCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public CapitalOverviewResponse getCapitalOverview(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        TimeCapitalResponse timeCapital = timeCapitalRepository.findByCapitalCycleId(cycleId)
                .map(capitalMapper::toTimeResponse)
                .orElseGet(() -> capitalMapper.uninitializedTimeResponse(cycleId));
        MoneyCapitalResponse moneyCapital = moneyCapitalRepository.findByCapitalCycleId(cycleId)
                .map(capitalMapper::toMoneyResponse)
                .orElseGet(() -> capitalMapper.uninitializedMoneyResponse(cycleId));

        return capitalMapper.toOverview(cycle, timeCapital, moneyCapital);
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId, ownerId));
    }

    private void ensureTimeCapitalNotInitialized(UUID cycleId) {
        if (timeCapitalRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.TIME);
        }
    }

    private void ensureMoneyCapitalNotInitialized(UUID cycleId) {
        if (moneyCapitalRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.MONEY);
        }
    }

    private TimeCapital findTimeCapital(UUID cycleId) {
        return timeCapitalRepository.findByCapitalCycleId(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
    }

    private MoneyCapital findMoneyCapital(UUID cycleId) {
        return moneyCapitalRepository.findByCapitalCycleId(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
    }
}
