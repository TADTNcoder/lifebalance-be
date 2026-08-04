package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.common.error.AppException;
import org.springframework.http.HttpStatus;

public class InvalidAllocationAmountException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_INVALID_ALLOCATION_AMOUNT";

    public InvalidAllocationAmountException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }
}
