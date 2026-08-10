package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalCycleNotFoundException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_NOT_FOUND";

    public CapitalCycleNotFoundException(UUID cycleId) {
        super(
                ERROR_CODE,
                "Capital cycle " + cycleId + " was not found.",
                HttpStatus.NOT_FOUND
        );
    }
}
