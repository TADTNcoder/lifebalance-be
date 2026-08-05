package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PersistenceCapitalAllocationReader implements CapitalAllocationReader {

    private static final int MONEY_SCALE = 4;

    private final CapitalAllocationRepository capitalAllocationRepository;

    public PersistenceCapitalAllocationReader(CapitalAllocationRepository capitalAllocationRepository) {
        this.capitalAllocationRepository = capitalAllocationRepository;
    }

    @Override
    public long getAllocatedMinutes(UUID cycleId) {
        return sum(cycleId, CapitalKind.TIME).longValueExact();
    }

    @Override
    public BigDecimal getAllocatedAmount(UUID cycleId) {
        return sum(cycleId, CapitalKind.MONEY);
    }

    private BigDecimal sum(UUID cycleId, CapitalKind capitalKind) {
        BigDecimal amount = capitalAllocationRepository.sumAllocatedAmount(cycleId, capitalKind);
        if (amount == null) {
            return zero();
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
