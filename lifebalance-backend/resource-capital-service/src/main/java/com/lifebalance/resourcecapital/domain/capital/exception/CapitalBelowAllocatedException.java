package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class CapitalBelowAllocatedException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_BELOW_ALLOCATED";

    public CapitalBelowAllocatedException(
            UUID cycleId,
            CapitalKind capitalKind,
            BigDecimal afterAmount,
            BigDecimal allocatedAmount
    ) {
        super(
                ERROR_CODE,
                capitalKind + " capital adjustment for cycle " + cycleId
                        + " would reduce planned capital to " + afterAmount
                        + ", below allocated capital " + allocatedAmount + ".",
                HttpStatus.CONFLICT
        );
    }
}
