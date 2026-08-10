package com.lifebalance.resourcecapital.dto;

public record RemainingTimeCapitalResponse(
        Long plannedMinutes,
        Long allocatedMinutes,
        Long remainingMinutes,
        boolean overAllocated,
        boolean initialized
) {
}
