package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;

import java.util.UUID;

public class CapitalNotSetupException extends RuntimeException {

    public CapitalNotSetupException(UUID cycleId, CapitalKind capitalKind) {
        super(capitalKind + " capital has not been setup for capital cycle " + cycleId + ".");
    }
}
