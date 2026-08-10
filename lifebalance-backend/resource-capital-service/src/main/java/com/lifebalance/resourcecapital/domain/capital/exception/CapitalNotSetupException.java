package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalNotSetupException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_NOT_SETUP";

    public CapitalNotSetupException(UUID cycleId, CapitalKind capitalKind) {
        super(
                ERROR_CODE,
                capitalKind + " capital has not been setup for capital cycle " + cycleId + ".",
                HttpStatus.CONFLICT
        );
    }
}
