package com.lifebalance.resourcecapital.domain.capitalhistory.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

public class InvalidCapitalHistoryFilterException extends CapitalDomainException {

    public static final String ERROR_CODE = "INVALID_CAPITAL_HISTORY_FILTER";

    public InvalidCapitalHistoryFilterException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }
}
