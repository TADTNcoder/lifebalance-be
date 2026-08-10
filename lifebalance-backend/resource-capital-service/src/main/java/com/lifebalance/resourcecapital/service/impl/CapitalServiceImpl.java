package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.resourcecapital.service.mapper.CapitalMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalServiceImpl implements CapitalService {

    private static final int MONEY_SCALE = 4;

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalHistoryRepository capitalHistoryRepository;
    private final CapitalMapper capitalMapper;

    public CapitalServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalHistoryRepository capitalHistoryRepository,
            CapitalMapper capitalMapper
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
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
            TimeCapital savedCapital = timeCapitalRepository.saveAndFlush(timeCapital);
            recordSetupHistory(
                    cycle,
                    CapitalKind.TIME,
                    money(savedCapital.getPlannedMinutes()),
                    "Setup time capital",
                    ownerId
            );
            return capitalMapper.toTimeResponse(savedCapital);
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
            MoneyCapital savedCapital = moneyCapitalRepository.saveAndFlush(moneyCapital);
            recordSetupHistory(
                    cycle,
                    CapitalKind.MONEY,
                    savedCapital.getPlannedAmount(),
                    "Setup money capital",
                    ownerId
            );
            return capitalMapper.toMoneyResponse(savedCapital);
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
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
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

    private void recordSetupHistory(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal amount,
            String reason,
            UUID actorId
    ) {
        capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                capitalType,
                CapitalActionType.CAPITAL_SET,
                amount,
                money(0),
                amount,
                reason,
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                actorId
        ));
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
