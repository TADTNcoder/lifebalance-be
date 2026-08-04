package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;

import java.math.BigDecimal;
import java.util.UUID;

public record ResourceBreakdownDto(
        CapitalKind capitalType,
        AllocationTargetType targetType,
        UUID targetId,
        BigDecimal allocatedAmount,
        BigDecimal percentageOfTotal,
        BigDecimal percentageOfAllocated
) {
}
