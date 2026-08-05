package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class OverAllocationNotAllowedException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_OVER_ALLOCATION_NOT_ALLOWED";

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
                HttpStatus.BAD_REQUEST
        );
    }
}
