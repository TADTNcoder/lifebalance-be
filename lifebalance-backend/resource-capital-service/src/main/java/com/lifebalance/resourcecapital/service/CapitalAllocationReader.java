package com.lifebalance.resourcecapital.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface CapitalAllocationReader {

    long getAllocatedMinutes(UUID cycleId);

    BigDecimal getAllocatedAmount(UUID cycleId);
}
