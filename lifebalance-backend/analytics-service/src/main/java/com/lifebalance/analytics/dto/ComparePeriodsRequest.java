package com.lifebalance.analytics.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ComparePeriodsRequest(
        @NotNull LocalDate baselineStart,
        @NotNull LocalDate baselineEnd,
        @NotNull LocalDate comparisonStart,
        @NotNull LocalDate comparisonEnd,
        @Size(min = 3, max = 3) String currencyCode
) {
}
