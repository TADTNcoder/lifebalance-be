package com.lifebalance.identity.dto;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cached user authorization data")
public record UserAuthorizationSnapshot(
        @Schema(description = "User id", example = "6f44f86a-66df-4b0d-b258-571a3a63fce1")
        UUID userId,
        @Schema(description = "Assigned role codes", example = "[\"USER\"]")
        Set<String> roles,
        @Schema(description = "Effective permission codes", example = "[\"user:read\", \"task:create\"]")
        Set<String> permissions
) {

    public UserAuthorizationSnapshot {
        roles = immutableSet(roles);
        permissions = immutableSet(permissions);
    }

    private static Set<String> immutableSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }
}
