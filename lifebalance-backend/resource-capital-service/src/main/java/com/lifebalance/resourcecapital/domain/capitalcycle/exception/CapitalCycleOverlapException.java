package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import java.time.LocalDate;
import java.util.UUID;

public class CapitalCycleOverlapException extends RuntimeException {

    public CapitalCycleOverlapException(UUID ownerId, LocalDate startDate, LocalDate endDate) {
        super("Capital cycle for owner " + ownerId + " overlaps period "
                + startDate + " to " + endDate + ".");
    }
}
