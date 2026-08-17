package com.lifebalance.resourcecapital.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface CapitalAllocationReader {

    long getAllocatedMinutes(UUID ownerId, UUID cycleId);

    BigDecimal getAllocatedAmount(UUID ownerId, UUID cycleId);

    long getAllocatedMinutes(UUID cycleId);

    BigDecimal getAllocatedAmount(UUID cycleId);
}
