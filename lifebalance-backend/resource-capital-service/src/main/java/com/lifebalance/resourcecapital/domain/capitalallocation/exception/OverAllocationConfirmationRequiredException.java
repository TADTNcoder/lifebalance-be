package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class OverAllocationConfirmationRequiredException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_OVER_ALLOCATION_CONFIRMATION_REQUIRED";

    public OverAllocationConfirmationRequiredException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal plannedAmount,
            BigDecimal projectedAllocatedAmount
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation for cycle " + cycleId
                        + " would exceed planned capital " + plannedAmount
                        + " with projected allocation " + projectedAllocatedAmount
                        + "; explicit over-allocation confirmation is required.",
                HttpStatus.CONFLICT
        );
    }
}
