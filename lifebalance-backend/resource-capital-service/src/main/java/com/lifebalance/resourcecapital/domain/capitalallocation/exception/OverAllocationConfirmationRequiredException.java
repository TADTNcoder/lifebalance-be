package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.OverAllocationConfirmation;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class OverAllocationConfirmationRequiredException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_OVER_ALLOCATION_CONFIRMATION_REQUIRED";

    private static final String DEFAULT_OPERATION_TYPE = "CAPITAL_CHANGE";
    private static final String DEFAULT_OPERATION_REFERENCE = "CAPITAL_CYCLE";

    public OverAllocationConfirmationRequiredException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal availableAmount,
            BigDecimal requestedAmount,
            BigDecimal projectedRemainingAmount
    ) {
        this(
                cycleId,
                capitalType,
                availableAmount,
                requestedAmount,
                projectedRemainingAmount,
                DEFAULT_OPERATION_TYPE,
                DEFAULT_OPERATION_REFERENCE
        );
    }

    public OverAllocationConfirmationRequiredException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal availableAmount,
            BigDecimal requestedAmount,
            BigDecimal projectedRemainingAmount,
            String operationType,
            String operationReference
    ) {
        super(
                ERROR_CODE,
                capitalType + " capital change for cycle " + cycleId
                        + " would exceed available capital. Available amount: " + availableAmount
                        + ", requested amount: " + requestedAmount
                        + ", projected remaining amount: " + projectedRemainingAmount
                        + ". A negative remaining amount represents an over-allocation state, "
                        + "not additional available capital; explicit confirmation is required.",
                HttpStatus.CONFLICT,
                OverAllocationConfirmation.details(
                        cycleId,
                        capitalType,
                        availableAmount,
                        requestedAmount,
                        projectedRemainingAmount,
                        operationType,
                        operationReference,
                        OverAllocationConfirmation.confirmationKey(
                                operationType,
                                cycleId,
                                capitalType,
                                operationReference,
                                requestedAmount,
                                availableAmount,
                                projectedRemainingAmount
                        )
                )
        );
    }

    public OverAllocationConfirmationRequiredException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal plannedAmount,
            BigDecimal projectedAllocatedAmount
    ) {
        this(
                cycleId,
                capitalType,
                plannedAmount,
                projectedAllocatedAmount,
                DEFAULT_OPERATION_TYPE,
                DEFAULT_OPERATION_REFERENCE
        );
    }

    public OverAllocationConfirmationRequiredException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal plannedAmount,
            BigDecimal projectedAllocatedAmount,
            String operationType,
            String operationReference
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation for cycle " + cycleId
                        + " would exceed planned capital " + plannedAmount
                        + " with projected allocation " + projectedAllocatedAmount
                        + "; explicit over-allocation confirmation is required.",
                HttpStatus.CONFLICT,
                OverAllocationConfirmation.details(
                        cycleId,
                        capitalType,
                        plannedAmount,
                        projectedAllocatedAmount,
                        plannedAmount.subtract(projectedAllocatedAmount),
                        operationType,
                        operationReference,
                        OverAllocationConfirmation.confirmationKey(
                                operationType,
                                cycleId,
                                capitalType,
                                operationReference,
                                projectedAllocatedAmount,
                                plannedAmount,
                                plannedAmount.subtract(projectedAllocatedAmount)
                        )
                )
        );
    }
}
