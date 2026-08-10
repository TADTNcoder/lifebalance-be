package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AllocationNotFoundException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_NOT_FOUND";

    public AllocationNotFoundException(
            UUID cycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation was not found for the requested target.",
                HttpStatus.NOT_FOUND
        );
    }
}
