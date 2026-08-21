package com.lifebalance.resourcecapital.integration;

import com.lifebalance.resourcecapital.dto.AllocationResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface CapitalIntegrationPublisher {

    void publishOverAllocationApproved(UUID actorId, AllocationResponse response, String action, String reason);

    void publishAdjustmentOverAllocationApproved(
            UUID actorId,
            UUID cycleId,
            String capitalType,
            String action,
            BigDecimal requestedAmount,
            BigDecimal remainingAmount,
            String reason
    );
}
