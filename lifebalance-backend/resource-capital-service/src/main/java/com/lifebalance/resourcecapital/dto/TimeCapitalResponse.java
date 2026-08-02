package com.lifebalance.resourcecapital.dto;

import java.util.UUID;

public record TimeCapitalResponse(
        UUID id,
        UUID cycleId,
        Long plannedMinutes,
        Long allocatedMinutes,
        Long availableMinutes,
        Long remainingMinutes,
        boolean initialized
) {
}
