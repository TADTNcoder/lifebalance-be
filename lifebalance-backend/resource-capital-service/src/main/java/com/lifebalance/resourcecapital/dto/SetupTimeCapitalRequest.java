package com.lifebalance.resourcecapital.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SetupTimeCapitalRequest(
        @NotNull
        @PositiveOrZero
        Long plannedMinutes
) {
}
