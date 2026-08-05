package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class CapitalExceededException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_EXCEEDED";

    public CapitalExceededException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal plannedAmount,
            BigDecimal projectedAllocatedAmount
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation for cycle " + cycleId
                        + " would allocate " + projectedAllocatedAmount
                        + ", exceeding planned capital " + plannedAmount + ".",
                HttpStatus.BAD_REQUEST
        );
    }
}
