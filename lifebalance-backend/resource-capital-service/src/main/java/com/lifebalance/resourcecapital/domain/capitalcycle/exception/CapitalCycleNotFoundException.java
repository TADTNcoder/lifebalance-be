package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import java.util.UUID;

public class CapitalCycleNotFoundException extends RuntimeException {

    public CapitalCycleNotFoundException(UUID cycleId, UUID ownerId) {
        super("Capital cycle " + cycleId + " was not found for owner " + ownerId + ".");
    }
}
