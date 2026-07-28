package com.lifebalance.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Role synchronization result")
public record RoleSyncResponse(
        @Schema(description = "Number of roles submitted to Keycloak", example = "3")
        int syncedRoles
) {
}
