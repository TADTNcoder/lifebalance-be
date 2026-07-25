package com.lifebalance.identity.dto;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record UserAuthorizationSnapshot(
        UUID userId,
        Set<String> roles,
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
