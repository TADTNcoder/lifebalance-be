package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

public class InvalidAllocationTargetException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_INVALID_ALLOCATION_TARGET";

    public InvalidAllocationTargetException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }
}
