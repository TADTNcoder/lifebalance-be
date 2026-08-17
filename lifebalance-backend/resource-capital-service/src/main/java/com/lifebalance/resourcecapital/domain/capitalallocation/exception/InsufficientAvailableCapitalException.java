package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientAvailableCapitalException extends CapitalDomainException {

    public static final String ERROR_CODE = "INSUFFICIENT_AVAILABLE_CAPITAL";

    public InsufficientAvailableCapitalException(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal availableAmount,
            BigDecimal requestedAmount
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation for cycle " + cycleId
                        + " exceeds available capital. Available amount: " + availableAmount
                        + ", requested amount: " + requestedAmount + ".",
                HttpStatus.CONFLICT
        );
    }
}
