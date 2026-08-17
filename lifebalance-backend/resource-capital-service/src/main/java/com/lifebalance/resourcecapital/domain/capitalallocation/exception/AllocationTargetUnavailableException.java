package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

public class AllocationTargetUnavailableException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_TARGET_UNAVAILABLE";

    public AllocationTargetUnavailableException(String message, Throwable cause) {
        super(ERROR_CODE, message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}
