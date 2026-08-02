package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;

import java.util.UUID;

public class CapitalAlreadyInitializedException extends RuntimeException {

    public CapitalAlreadyInitializedException(UUID cycleId, CapitalKind capitalKind) {
        super(capitalKind + " capital has already been initialized for capital cycle " + cycleId + ".");
    }

    public CapitalAlreadyInitializedException(UUID cycleId, CapitalKind capitalKind, Throwable cause) {
        super(capitalKind + " capital has already been initialized for capital cycle " + cycleId + ".", cause);
    }
}
