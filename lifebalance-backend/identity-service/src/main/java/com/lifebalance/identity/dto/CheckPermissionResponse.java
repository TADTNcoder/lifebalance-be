package com.lifebalance.identity.dto;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.model.enums.AccountStatus;

public record CheckPermissionResponse(
        boolean authenticated,
        UUID userId,
        String keycloakId,
        String username,
        String email,
        String displayName,
        AccountStatus status,
        List<String> tokenRoles,
        List<String> roles,
        List<String> permissions,
        String requestedPermission,
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
