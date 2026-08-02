package com.lifebalance.identity.dto;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.model.enums.AccountStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current authenticated user authorization snapshot")
public record CheckPermissionResponse(
        @Schema(description = "Whether the bearer token was authenticated", example = "true")
        boolean authenticated,
        @Schema(description = "Internal LifeBalance user id", example = "6f44f86a-66df-4b0d-b258-571a3a63fce1")
        UUID userId,
        @Schema(description = "Keycloak subject id", example = "e0a721b8-0ffd-45f8-a8a1-8c3a7eb2a676")
        String keycloakId,
        @Schema(description = "Username from the mapped identity", example = "alice")
        String username,
        @Schema(description = "User email address", example = "alice@example.com")
        String email,
        @Schema(description = "Display name shown in LifeBalance", example = "Alice Nguyen")
        String displayName,
        @Schema(description = "Current account status", example = "ACTIVE")
        AccountStatus status,
        @Schema(description = "Roles present in the access token", example = "[\"user\"]")
        List<String> tokenRoles,
        @Schema(description = "Application roles assigned to the user", example = "[\"USER\"]")
        List<String> roles,
        @Schema(description = "Effective application permissions", example = "[\"user:read\", \"task:create\"]")
        List<String> permissions,
        @Schema(description = "Permission supplied in the permission query parameter", example = "user:read")
        String requestedPermission,
        @Schema(description = "Whether requestedPermission is granted; null when no permission was requested", example = "true")
        Boolean hasPermission
) {

    public CheckPermissionResponse {
        tokenRoles = immutableList(tokenRoles);
        roles = immutableList(roles);
        permissions = immutableList(permissions);
    }

    private static List<String> immutableList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return List.copyOf(values);
    }
}
