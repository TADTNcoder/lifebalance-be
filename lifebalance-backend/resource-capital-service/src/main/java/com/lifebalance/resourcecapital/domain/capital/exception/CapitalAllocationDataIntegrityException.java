package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalAllocationDataIntegrityException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_DATA_INTEGRITY_ERROR";

    public CapitalAllocationDataIntegrityException(UUID cycleId, CapitalKind capitalKind) {
        super(
                ERROR_CODE,
                capitalKind + " allocation data has an invalid persisted format for capital cycle " + cycleId + ".",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
