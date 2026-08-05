package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AllocationNotFoundException extends AppException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_NOT_FOUND";

    public AllocationNotFoundException(
            UUID cycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId
    ) {
        super(
                ERROR_CODE,
                capitalType + " allocation for " + targetType + " " + targetId
                        + " was not found in capital cycle " + cycleId + ".",
                HttpStatus.NOT_FOUND
        );
    }
}
