package com.lifebalance.resourcecapital.domain.capitalallocation.exception;

import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidAllocationStateException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_ALLOCATION_INVALID_STATE";

    public InvalidAllocationStateException(UUID allocationId, AllocationStatus status, String action) {
        super(
                ERROR_CODE,
                "Capital allocation " + allocationId
                        + " is in status " + status
                        + " and cannot " + action
                        + ". Only ACTIVE allocations can be released.",
                HttpStatus.CONFLICT
        );
    }
}
