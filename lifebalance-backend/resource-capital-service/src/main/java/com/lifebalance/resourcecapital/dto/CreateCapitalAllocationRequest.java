package com.lifebalance.resourcecapital.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCapitalAllocationRequest(
        @NotNull
        UUID capitalCycleId,

        @NotNull
        @JsonAlias("resourceType")
        CapitalKind capitalType,

        @NotNull
        AllocationTargetType targetType,

        UUID targetId,

        UUID taskId,

        UUID taskCatalogId,

        UUID projectId,

        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @JsonAlias("overAllocationConfirmed")
        boolean allowOverAllocation,

        @Size(max = 1000)
        String reason
) {
}
