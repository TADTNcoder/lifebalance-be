package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientAllocatedCapitalException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_INSUFFICIENT_ALLOCATED";

    public InsufficientAllocatedCapitalException(
            UUID cycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            BigDecimal requestedAmount,
            BigDecimal allocatedAmount
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation for " + targetType + " " + targetId
                        + " in cycle " + cycleId
                        + " has allocated amount " + allocatedAmount
                        + ", less than requested amount " + requestedAmount + ".",
                HttpStatus.BAD_REQUEST
        );
    }
}
