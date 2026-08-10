package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalCycleOwnershipException extends CapitalDomainException {

    public CapitalCycleOwnershipException(UUID cycleId, UUID expectedOwnerId) {
        super(
                CapitalCycleNotFoundException.ERROR_CODE,
                "Capital cycle " + cycleId + " was not found.",
                HttpStatus.NOT_FOUND
        );
    }
}
