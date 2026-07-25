package com.lifebalance.identity.security;

import java.util.Collection;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class PermissionMatcher {

    private static final String GLOBAL_WILDCARD = "*";
    private static final String GLOBAL_DOMAIN_ACTION_WILDCARD = "*:*";
    private static final String SEPARATOR = ":";
    private static final String WILDCARD_ACTION = "*";

    public boolean anyMatches(
            Collection<String> grantedPermissions,
            String requiredPermission
    ) {
        String normalizedRequiredPermission = normalize(requiredPermission);
        if (grantedPermissions == null
                || grantedPermissions.isEmpty()
                || normalizedRequiredPermission == null) {
            return false;
        }

        return grantedPermissions.stream()
                .anyMatch(grantedPermission ->
                        matches(grantedPermission, normalizedRequiredPermission)
                );
    }

    public boolean matches(
            String grantedPermission,
            String requiredPermission
    ) {
        String normalizedGrantedPermission = normalize(grantedPermission);
        String normalizedRequiredPermission = normalize(requiredPermission);
        if (normalizedGrantedPermission == null || normalizedRequiredPermission == null) {
            return false;
        }

        if (GLOBAL_WILDCARD.equals(normalizedGrantedPermission)
                || GLOBAL_DOMAIN_ACTION_WILDCARD.equals(normalizedGrantedPermission)) {
            return true;
        }

        if (normalizedGrantedPermission.equals(normalizedRequiredPermission)) {
            return true;
        }

        String[] grantedParts = splitPermission(normalizedGrantedPermission);
        String[] requiredParts = splitPermission(normalizedRequiredPermission);
        if (grantedParts == null || requiredParts == null) {
            return false;
        }

        return grantedParts[0].equals(requiredParts[0])
                && WILDCARD_ACTION.equals(grantedParts[1]);
    }

    public String permissionKey(
            String targetDomain,
            String action
    ) {
        String normalizedTargetDomain = normalizeSegment(targetDomain);
        String normalizedAction = normalizeSegment(action);
        if (normalizedTargetDomain == null || normalizedAction == null) {
            return null;
        }

        return normalizedTargetDomain + SEPARATOR + normalizedAction;
    }

    private static String[] splitPermission(String permission) {
        String[] parts = permission.split(SEPARATOR, -1);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }

        return parts;
    }

    private static String normalizeSegment(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.contains(SEPARATOR)) {
            return null;
        }

        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
