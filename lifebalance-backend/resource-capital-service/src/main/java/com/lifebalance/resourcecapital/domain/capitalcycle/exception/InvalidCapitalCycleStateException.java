package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;

import java.util.UUID;

public class InvalidCapitalCycleStateException extends RuntimeException {

    public InvalidCapitalCycleStateException(UUID cycleId, CapitalCycleStatus currentStatus, String action, String reason) {
        super("Capital cycle " + cycleId + " with status " + currentStatus
                + " cannot " + action + ": " + reason);
    }
}
