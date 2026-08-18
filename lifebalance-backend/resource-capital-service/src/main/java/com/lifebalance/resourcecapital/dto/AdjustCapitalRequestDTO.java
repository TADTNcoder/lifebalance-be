package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record AdjustCapitalRequestDTO(
        @NotNull
        UUID capitalCycleId,

        @NotNull
        CapitalKind capitalType,

        @NotNull
        CapitalAdjustmentType adjustmentType,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @NotBlank
        @Size(max = 1000)
        String reason,

        boolean overAllocationConfirmed,

        @Size(max = 128)
        String overAllocationConfirmationKey
) {

    public AdjustCapitalRequestDTO(
            UUID capitalCycleId,
            CapitalKind capitalType,
            CapitalAdjustmentType adjustmentType,
            BigDecimal amount,
            String reason,
            boolean overAllocationConfirmed
    ) {
        this(capitalCycleId, capitalType, adjustmentType, amount, reason, overAllocationConfirmed, null);
    }

    public static AdjustCapitalRequestDTO from(CapitalAdjustmentRequest request) {
        Objects.requireNonNull(request, "Capital adjustment request is required.");
        return new AdjustCapitalRequestDTO(
                request.capitalCycleId(),
                request.capitalType(),
                request.adjustmentType(),
                request.amount(),
                request.reason(),
                request.allowOverAllocation(),
                request.overAllocationConfirmationKey()
        );
    }
}
