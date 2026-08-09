package com.lifebalance.resourcecapital.dto;

public record AvailableTimeCapitalResponse(
        Long availableMinutes,
        boolean initialized
) {
}
