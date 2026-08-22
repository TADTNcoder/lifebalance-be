package com.lifebalance.notification.controller;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

final class CurrentNotificationUser {

    private static final Set<String> NOTIFICATION_MANAGER_ROLES = Set.of("admin", "manager");

    private CurrentNotificationUser() {
    }

    static UUID ownerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }

    static void requireNotificationManager(KeycloakUserPrincipal currentUser, boolean trustedInternalRequest) {
        ownerId(currentUser);
        if (trustedInternalRequest || hasNotificationManagerRole(currentUser)) {
            return;
        }

        throw new AccessDeniedException("Notification management permission is required.");
    }

    static void requireAllowedChannels(
            KeycloakUserPrincipal currentUser,
            Set<NotificationChannel> channels,
            boolean trustedInternalRequest
    ) {
        ownerId(currentUser);
        if (trustedInternalRequest || hasNotificationManagerRole(currentUser)) {
            return;
        }
        if (channels == null || channels.isEmpty()
                || channels.stream().allMatch(NotificationChannel.IN_APP::equals)) {
            return;
        }

        throw new AccessDeniedException("External notification channels require notification management permission.");
    }

    private static boolean hasNotificationManagerRole(KeycloakUserPrincipal currentUser) {
        return currentUser.roles().stream()
                .map(CurrentNotificationUser::normalizeRole)
                .anyMatch(NOTIFICATION_MANAGER_ROLES::contains);
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("role_") || normalized.startsWith("role-")) {
            return normalized.substring(5);
        }
        return normalized;
    }
}
