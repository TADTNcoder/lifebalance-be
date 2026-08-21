package com.lifebalance.resourcecapital.domain.capital.exception;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.exception.CapitalDomainException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class InvalidCapitalTransferException extends CapitalDomainException {

    public static final String ERROR_CODE = "CAPITAL_INVALID_TRANSFER";

    public InvalidCapitalTransferException(String message, HttpStatus status, Map<String, String> details) {
        super(ERROR_CODE, message, status, details);
    }

    public static InvalidCapitalTransferException confirmationRequired(UUID sourceCycleId, UUID targetCycleId) {
        return new InvalidCapitalTransferException(
                "Transfer remaining capital requires explicit user confirmation.",
                HttpStatus.CONFLICT,
                Map.of(
                        "sourceCycleId", String.valueOf(sourceCycleId),
                        "targetCycleId", String.valueOf(targetCycleId)
                )
        );
    }

    public static InvalidCapitalTransferException sameCycle(UUID cycleId) {
        return new InvalidCapitalTransferException(
                "Source and target capital cycles must be different.",
                HttpStatus.BAD_REQUEST,
                Map.of("cycleId", String.valueOf(cycleId))
        );
    }

    public static InvalidCapitalTransferException sourceMustBeClosed(
            UUID sourceCycleId,
            CapitalCycleStatus currentStatus
    ) {
        return new InvalidCapitalTransferException(
                "Remaining capital can be transferred only from a closed source cycle.",
                HttpStatus.CONFLICT,
                Map.of(
                        "sourceCycleId", String.valueOf(sourceCycleId),
                        "status", String.valueOf(currentStatus)
                )
        );
    }

    public static InvalidCapitalTransferException targetMustBeFuture(UUID sourceCycleId, UUID targetCycleId) {
        return new InvalidCapitalTransferException(
                "Remaining capital can be transferred only to a future capital cycle.",
                HttpStatus.CONFLICT,
                Map.of(
                        "sourceCycleId", String.valueOf(sourceCycleId),
                        "targetCycleId", String.valueOf(targetCycleId)
                )
        );
    }

    public static InvalidCapitalTransferException remainingNotPositive(
            UUID sourceCycleId,
            CapitalKind capitalType,
            BigDecimal remaining
    ) {
        return new InvalidCapitalTransferException(
                "Source remaining capital must be positive before transfer.",
                HttpStatus.CONFLICT,
                Map.of(
                        "sourceCycleId", String.valueOf(sourceCycleId),
                        "capitalType", String.valueOf(capitalType),
                        "remaining", String.valueOf(remaining)
                )
        );
    }

    public static InvalidCapitalTransferException amountExceedsRemaining(
            UUID sourceCycleId,
            CapitalKind capitalType,
            BigDecimal availableRemaining,
            BigDecimal requestedAmount
    ) {
        return new InvalidCapitalTransferException(
                "Transfer amount must not exceed source remaining capital.",
                HttpStatus.CONFLICT,
                Map.of(
                        "sourceCycleId", String.valueOf(sourceCycleId),
                        "capitalType", String.valueOf(capitalType),
                        "availableRemaining", String.valueOf(availableRemaining),
                        "requestedAmount", String.valueOf(requestedAmount)
                )
        );
    }

    public static InvalidCapitalTransferException timeAmountMustBeWholeMinutes(BigDecimal amount) {
        return new InvalidCapitalTransferException(
                "Time transfer amount must be whole minutes.",
                HttpStatus.BAD_REQUEST,
                Map.of("amount", String.valueOf(amount))
        );
    }

    public static InvalidCapitalTransferException currencyMismatch(
            UUID sourceCycleId,
            UUID targetCycleId,
            String sourceCurrencyCode,
            String targetCurrencyCode
    ) {
        return new InvalidCapitalTransferException(
                "Money transfer requires matching source and target currency.",
                HttpStatus.CONFLICT,
                Map.of(
                        "sourceCycleId", String.valueOf(sourceCycleId),
                        "targetCycleId", String.valueOf(targetCycleId),
                        "sourceCurrencyCode", String.valueOf(sourceCurrencyCode),
                        "targetCurrencyCode", String.valueOf(targetCurrencyCode)
                )
        );
    }
}
