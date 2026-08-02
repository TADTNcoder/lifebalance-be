package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ActiveCapitalCycleAlreadyExistsException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_ACTIVE_ALREADY_EXISTS";

    public ActiveCapitalCycleAlreadyExistsException(UUID ownerId, CapitalCycleType type) {
        super(
                ERROR_CODE,
                "An active capital cycle already exists for cycle type " + type + ".",
                HttpStatus.CONFLICT
        );
    }
}
