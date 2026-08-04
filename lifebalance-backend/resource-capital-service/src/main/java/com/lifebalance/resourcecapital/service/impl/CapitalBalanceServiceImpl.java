package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceSummaryDto;
import com.lifebalance.resourcecapital.dto.ResourceBreakdownDto;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalBalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalBalanceServiceImpl implements CapitalBalanceService {

    private static final int MONEY_SCALE = 4;
    private static final int PERCENTAGE_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalAllocationRepository capitalAllocationRepository;

    public CapitalBalanceServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalAllocationRepository capitalAllocationRepository
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalAllocationRepository = capitalAllocationRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public CapitalBalanceResponse getCycleBalance(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        TimeCapital timeCapital = timeCapitalRepository.findByCapitalCycleId(cycleId).orElse(null);
        MoneyCapital moneyCapital = moneyCapitalRepository.findByCapitalCycleId(cycleId).orElse(null);

        return new CapitalBalanceResponse(
                cycle.getId(),
                cycle.getStatus(),
                timeBalance(cycleId, timeCapital),
                moneyBalance(cycleId, moneyCapital)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<ResourceBreakdownDto> getAllocationBreakdownByTarget(
            UUID ownerId,
            UUID cycleId,
            AllocationTargetType targetType
    ) {
        Objects.requireNonNull(targetType, "Allocation target type is required.");
        findOwnedCycle(ownerId, cycleId);

        Map<CapitalKind, BigDecimal> totals = plannedTotals(cycleId);
        Map<CapitalKind, BigDecimal> allocatedTotals = allocatedTotals(cycleId);
        return capitalAllocationRepository.findAllocationBreakdownByTargetType(cycleId, targetType)
                .stream()
                .map(item -> toBreakdown(item, totals, allocatedTotals))
                .toList();
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId, ownerId));
    }

    private CapitalBalanceSummaryDto timeBalance(UUID cycleId, TimeCapital timeCapital) {
        BigDecimal total = timeCapital == null ? zeroMoney() : money(timeCapital.getPlannedMinutes());
        BigDecimal allocated = sumAllocated(cycleId, CapitalKind.TIME);
        return balance(CapitalKind.TIME, total, allocated, null, timeCapital != null);
    }

    private CapitalBalanceSummaryDto moneyBalance(UUID cycleId, MoneyCapital moneyCapital) {
        BigDecimal total = moneyCapital == null ? zeroMoney() : moneyCapital.getPlannedAmount();
        BigDecimal allocated = sumAllocated(cycleId, CapitalKind.MONEY);
        String currencyCode = moneyCapital == null ? null : moneyCapital.getCurrencyCode();
        return balance(CapitalKind.MONEY, total, allocated, currencyCode, moneyCapital != null);
    }

    private CapitalBalanceSummaryDto balance(
            CapitalKind capitalType,
            BigDecimal total,
            BigDecimal allocated,
            String currencyCode,
            boolean initialized
    ) {
        BigDecimal available = total.subtract(allocated).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        return new CapitalBalanceSummaryDto(
                capitalType,
                total,
                allocated,
                available,
                available,
                percentage(allocated, total),
                allocated.compareTo(total) > 0,
                currencyCode,
                initialized
        );
    }

    private ResourceBreakdownDto toBreakdown(
            CapitalAllocationRepository.TargetAllocationBreakdownProjection item,
            Map<CapitalKind, BigDecimal> totals,
            Map<CapitalKind, BigDecimal> allocatedTotals
    ) {
        BigDecimal allocatedAmount = normalize(item.getAllocatedAmount());
        CapitalKind capitalType = item.getCapitalType();
        return new ResourceBreakdownDto(
                capitalType,
                item.getTargetType(),
                item.getTargetId(),
                allocatedAmount,
                percentage(allocatedAmount, totals.getOrDefault(capitalType, zeroMoney())),
                percentage(allocatedAmount, allocatedTotals.getOrDefault(capitalType, zeroMoney()))
        );
    }

    private Map<CapitalKind, BigDecimal> plannedTotals(UUID cycleId) {
        Map<CapitalKind, BigDecimal> totals = new EnumMap<>(CapitalKind.class);
        totals.put(CapitalKind.TIME, timeCapitalRepository.findByCapitalCycleId(cycleId)
                .map(TimeCapital::getPlannedMinutes)
                .map(this::money)
                .orElseGet(this::zeroMoney));
        totals.put(CapitalKind.MONEY, moneyCapitalRepository.findByCapitalCycleId(cycleId)
                .map(MoneyCapital::getPlannedAmount)
                .orElseGet(this::zeroMoney));
        return totals;
    }

    private Map<CapitalKind, BigDecimal> allocatedTotals(UUID cycleId) {
        Map<CapitalKind, BigDecimal> totals = new EnumMap<>(CapitalKind.class);
        totals.put(CapitalKind.TIME, sumAllocated(cycleId, CapitalKind.TIME));
        totals.put(CapitalKind.MONEY, sumAllocated(cycleId, CapitalKind.MONEY));
        return totals;
    }

    private BigDecimal sumAllocated(UUID cycleId, CapitalKind capitalType) {
        return normalize(capitalAllocationRepository.sumAllocatedAmount(cycleId, capitalType));
    }

    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(PERCENTAGE_SCALE, RoundingMode.UNNECESSARY);
        }
        return numerator.multiply(HUNDRED).divide(denominator, PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return zeroMoney();
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
