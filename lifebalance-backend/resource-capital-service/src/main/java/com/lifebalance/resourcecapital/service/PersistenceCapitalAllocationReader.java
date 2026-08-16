package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PersistenceCapitalAllocationReader implements CapitalAllocationReader {

    private static final int MONEY_SCALE = 4;

    private final CapitalCycleRepository capitalCycleRepository;
    private final CapitalAllocationRepository capitalAllocationRepository;

    public PersistenceCapitalAllocationReader(
            CapitalCycleRepository capitalCycleRepository,
            CapitalAllocationRepository capitalAllocationRepository
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.capitalAllocationRepository = capitalAllocationRepository;
    }

    @Override
    public long getAllocatedMinutes(UUID ownerId, UUID cycleId) {
        return sum(ownerId, cycleId, CapitalKind.TIME).longValueExact();
    }

    @Override
    public BigDecimal getAllocatedAmount(UUID ownerId, UUID cycleId) {
        return sum(ownerId, cycleId, CapitalKind.MONEY);
    }

    @Override
    public long getAllocatedMinutes(UUID cycleId) {
        return getAllocatedMinutes(resolveOwnerId(cycleId), cycleId);
    }

    @Override
    public BigDecimal getAllocatedAmount(UUID cycleId) {
        return getAllocatedAmount(resolveOwnerId(cycleId), cycleId);
    }

    private BigDecimal sum(UUID ownerId, UUID cycleId, CapitalKind capitalKind) {
        BigDecimal amount = capitalAllocationRepository.sumAllocatedAmount(ownerId, cycleId, capitalKind);
        if (amount == null) {
            return zero();
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private UUID resolveOwnerId(UUID cycleId) {
        return capitalCycleRepository.findById(cycleId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId))
                .getOwnerId();
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
