package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidCapitalCycleStateException extends CapitalDomainException {

    public static final String ERROR_CODE = "INVALID_CYCLE_STATE_TRANSITION";

    private final UUID cycleId;
    private final CapitalCycleStatus currentStatus;
    private final CapitalCycleStatus requestedStatus;
    private final String action;

    public InvalidCapitalCycleStateException(UUID cycleId, CapitalCycleStatus currentStatus, String action, String reason) {
        this(cycleId, currentStatus, null, action, reason);
    }

    public InvalidCapitalCycleStateException(
            UUID cycleId,
            CapitalCycleStatus currentStatus,
            CapitalCycleStatus requestedStatus,
            String action
    ) {
        this(
                cycleId,
                currentStatus,
                requestedStatus,
                action,
                "transition from " + currentStatus + " to " + requestedStatus + " is not allowed"
        );
    }

    public InvalidCapitalCycleStateException(
            UUID cycleId,
            CapitalCycleStatus currentStatus,
            CapitalCycleStatus requestedStatus,
            String action,
            String reason
    ) {
        super(
                ERROR_CODE,
                "Capital cycle " + cycleId + " with status " + currentStatus
                        + " cannot " + action + ": " + reason,
                HttpStatus.BAD_REQUEST
        );
        this.cycleId = cycleId;
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
        this.action = action;
    }

    public UUID getCycleId() {
        return cycleId;
    }

    public CapitalCycleStatus getCurrentStatus() {
        return currentStatus;
    }

    public CapitalCycleStatus getRequestedStatus() {
        return requestedStatus;
    }

    public String getAction() {
        return action;
    }
}
