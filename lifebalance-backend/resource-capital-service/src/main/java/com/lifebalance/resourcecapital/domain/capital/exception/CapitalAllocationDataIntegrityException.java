package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalAllocationDataIntegrityException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_DATA_INTEGRITY_ERROR";

    public CapitalAllocationDataIntegrityException(UUID cycleId, CapitalKind capitalKind) {
        super(
                ERROR_CODE,
                "Capital allocation data is inconsistent.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
