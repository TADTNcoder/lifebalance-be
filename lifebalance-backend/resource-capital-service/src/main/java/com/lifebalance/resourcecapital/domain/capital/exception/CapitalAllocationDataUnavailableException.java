package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalAllocationDataUnavailableException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_DATA_UNAVAILABLE";

    public CapitalAllocationDataUnavailableException(UUID cycleId, CapitalKind capitalKind) {
        super(
                ERROR_CODE,
                capitalKind + " allocation data is unavailable for capital cycle " + cycleId + ".",
                HttpStatus.CONFLICT
        );
    }
}
