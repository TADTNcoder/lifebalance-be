package com.lifebalance.resourcecapital.domain.capitalcycle.exception;

import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class CapitalCycleDeletionNotAllowedException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_CYCLE_DELETION_NOT_ALLOWED";

    public CapitalCycleDeletionNotAllowedException(UUID cycleId, String reason) {
        super(
                ERROR_CODE,
                "Capital cycle " + cycleId + " cannot be deleted: " + reason,
                HttpStatus.CONFLICT,
                Map.of(
                        "cycleId", String.valueOf(cycleId),
                        "reason", reason
                )
        );
    }
}
