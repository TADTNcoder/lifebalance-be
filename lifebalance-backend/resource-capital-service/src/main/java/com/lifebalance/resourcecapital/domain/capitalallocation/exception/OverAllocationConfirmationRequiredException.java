package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class OverAllocationConfirmationRequiredException extends AppException {

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
                HttpStatus.BAD_REQUEST
        );
    }
}
