package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import java.util.UUID;

public class CapitalCycleOwnershipException extends RuntimeException {

    public CapitalCycleOwnershipException(UUID cycleId, UUID expectedOwnerId) {
        super("Capital cycle " + cycleId + " does not belong to owner " + expectedOwnerId + ".");
    }
}
