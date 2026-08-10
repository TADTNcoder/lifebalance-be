package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalAlreadyInitializedException extends AppException {

    public CapitalAlreadyInitializedException(UUID cycleId, CapitalKind capitalKind) {
        super(
                errorCode(capitalKind),
                capitalKind + " capital has already been initialized for capital cycle " + cycleId + ".",
                HttpStatus.CONFLICT
        );
    }

    public CapitalAlreadyInitializedException(UUID cycleId, CapitalKind capitalKind, Throwable cause) {
        this(cycleId, capitalKind);
        initCause(cause);
    }

    private static String errorCode(CapitalKind capitalKind) {
        return switch (capitalKind) {
            case TIME -> "TIME_CAPITAL_ALREADY_EXISTS";
            case MONEY -> "MONEY_CAPITAL_ALREADY_EXISTS";
        };
    }
}
