package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.common.error.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCapitalCyclePeriodException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_INVALID_PERIOD";

    public InvalidCapitalCyclePeriodException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }
}
