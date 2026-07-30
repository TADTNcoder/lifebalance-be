package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import java.util.UUID;

public class ActiveCapitalCycleAlreadyExistsException extends RuntimeException {

    public ActiveCapitalCycleAlreadyExistsException(UUID ownerId) {
        super("Owner " + ownerId + " already has an ACTIVE capital cycle.");
    }
}
