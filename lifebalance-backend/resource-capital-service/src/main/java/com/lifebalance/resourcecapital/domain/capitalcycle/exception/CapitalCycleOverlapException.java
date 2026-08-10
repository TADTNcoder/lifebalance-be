package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

public class CapitalCycleOverlapException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_OVERLAP";

    public CapitalCycleOverlapException(UUID ownerId, LocalDate startDate, LocalDate endDate) {
        super(
                ERROR_CODE,
                "Capital cycle overlaps the requested period.",
                HttpStatus.BAD_REQUEST
        );
    }
}
