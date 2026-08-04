package com.lifebalance.resourcecapital.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Temporary LB-835 adapter: allocation persistence is introduced by LB-836, so current allocated capital is zero.
 */
@Service
public class NoAllocationPersistenceReader implements CapitalAllocationReader {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(4, RoundingMode.UNNECESSARY);

    @Override
    public long getAllocatedMinutes(UUID cycleId) {
        return 0L;
    }

    @Override
    public BigDecimal getAllocatedAmount(UUID cycleId) {
        return ZERO_MONEY;
    }
}
