package com.lifebalance.identity.dto;

import lombok.Builder;

@Builder
public record MaintenanceStatusResponse(
        boolean policyEnabled,
        boolean maintenanceMode,
        String message
) {
}
