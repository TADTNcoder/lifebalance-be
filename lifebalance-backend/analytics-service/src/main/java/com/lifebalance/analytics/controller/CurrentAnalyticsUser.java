package com.lifebalance.analytics.controller;

import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

final class CurrentAnalyticsUser {

    private CurrentAnalyticsUser() {
    }

    static UUID ownerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
