package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.common.error.AppException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

public class CapitalCycleOverlapException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_OVERLAP";

    public CapitalCycleOverlapException(UUID ownerId, LocalDate startDate, LocalDate endDate) {
        super(
                ERROR_CODE,
                "Capital cycle for owner " + ownerId + " overlaps period "
                        + startDate + " to " + endDate + ".",
                HttpStatus.BAD_REQUEST
        );
    }
}
