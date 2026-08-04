package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.common.error.AppException;
import org.springframework.http.HttpStatus;

public class InvalidAdjustmentAmountException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_INVALID_ADJUSTMENT_AMOUNT";

    public InvalidAdjustmentAmountException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }

    public InvalidAdjustmentAmountException(String message, Throwable cause) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
        initCause(cause);
    }

    public static InvalidAdjustmentAmountException nonPositiveTime(long amountInMinutes) {
        return new InvalidAdjustmentAmountException(
                "Time adjustment amount must be greater than zero minutes, but was " + amountInMinutes + "."
        );
    }

    public static InvalidAdjustmentAmountException timeBelowZero(long currentMinutes, long amountInMinutes) {
        return new InvalidAdjustmentAmountException(
                "Cannot decrease time capital by " + amountInMinutes
                        + " minutes from current planned minutes " + currentMinutes + "."
        );
    }

    public static InvalidAdjustmentAmountException timeOverflow(long amountInMinutes, Throwable cause) {
        return new InvalidAdjustmentAmountException(
                "Increasing time capital by " + amountInMinutes + " minutes exceeds the supported range.",
                cause
        );
    }

    public static InvalidAdjustmentAmountException invalidMoney(String detail) {
        return new InvalidAdjustmentAmountException("Invalid money adjustment amount: " + detail);
    }
}
