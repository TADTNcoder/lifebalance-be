package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class OverAllocationNotAllowedException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_OVER_ALLOCATION_NOT_ALLOWED";

    public OverAllocationNotAllowedException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal availableAmount,
            BigDecimal requestedAmount,
            BigDecimal projectedRemainingAmount
    ) {
        super(
                ERROR_CODE,
                capitalType + " capital change for cycle " + cycleId
                        + " would exceed available capital. Available amount: " + availableAmount
                        + ", requested amount: " + requestedAmount
                        + ", projected remaining amount: " + projectedRemainingAmount
                        + ". A negative remaining amount represents an over-allocation state, "
                        + "but over-allocation is disabled for the cycle.",
                HttpStatus.CONFLICT
        );
    }

    public OverAllocationNotAllowedException(
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
                        + ", but over-allocation is disabled for the cycle.",
                HttpStatus.CONFLICT
        );
    }
}
