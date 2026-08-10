package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CapitalSummaryResponseDTO(
        boolean activeCyclePresent,
        UUID activeCycleId,
        CapitalCycleType activeCycleType,
        CapitalCycleStatus activeCycleStatus,
        LocalDate activeCycleStartDate,
        LocalDate activeCycleEndDate,
        TimeCapitalSummaryDTO timeCapital,
        MoneyCapitalSummaryDTO moneyCapital
) {

    public record TimeCapitalSummaryDTO(
            BigDecimal allocatedHours,
            BigDecimal spentHours,
            BigDecimal remainingHours,
            Long allocatedMinutes,
            Long spentMinutes,
            Long remainingMinutes,
            boolean initialized,
            boolean overAllocated
    ) {
    }

    public record MoneyCapitalSummaryDTO(
            BigDecimal allocatedAmount,
            BigDecimal spentAmount,
            BigDecimal remainingAmount,
            String currencyCode,
            boolean initialized,
            boolean overAllocated
    ) {
    }
}
