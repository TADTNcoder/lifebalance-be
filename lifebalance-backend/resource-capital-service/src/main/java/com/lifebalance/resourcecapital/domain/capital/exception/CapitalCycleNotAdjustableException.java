package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CapitalCycleNotAdjustableException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_NOT_ADJUSTABLE";

    public CapitalCycleNotAdjustableException(UUID cycleId, CapitalCycleStatus status) {
        super(
                ERROR_CODE,
                "Capital cycle " + cycleId + " with status " + status + " does not allow capital adjustment.",
                HttpStatus.CONFLICT
        );
    }
}
