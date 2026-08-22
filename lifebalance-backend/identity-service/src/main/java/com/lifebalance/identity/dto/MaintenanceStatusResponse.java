package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record MaintenanceStatusResponse(
        boolean policyEnabled,
        boolean maintenanceMode,
        String message,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
) {
}
