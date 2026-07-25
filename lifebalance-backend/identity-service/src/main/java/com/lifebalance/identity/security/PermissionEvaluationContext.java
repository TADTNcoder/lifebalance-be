package com.lifebalance.identity.security;

import java.util.Map;
import java.util.UUID;

public record PermissionEvaluationContext(
        String targetDomain,
        String action,
        UUID resourceOwnerUserId,
        Map<String, Object> attributes
) {

    public PermissionEvaluationContext {
        attributes = attributes == null || attributes.isEmpty()
                ? Map.of()
                : Map.copyOf(attributes);
    }

    public static PermissionEvaluationContext of(String targetDomain, String action) {
        return new PermissionEvaluationContext(targetDomain, action, null, Map.of());
    }

    public static PermissionEvaluationContext ownedBy(
            String targetDomain,
            String action,
            UUID resourceOwnerUserId
    ) {
        return new PermissionEvaluationContext(targetDomain, action, resourceOwnerUserId, Map.of());
    }
}
