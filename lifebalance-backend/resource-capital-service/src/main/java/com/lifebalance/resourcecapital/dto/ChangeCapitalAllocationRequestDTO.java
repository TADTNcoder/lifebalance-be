package com.lifebalance.resourcecapital.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ChangeCapitalAllocationRequestDTO(
        @NotNull
        UUID allocationId,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 15, fraction = 4)
        BigDecimal newAmount,

        boolean overAllocationConfirmed,

        @Size(max = 1000)
        String reason
) {
}
