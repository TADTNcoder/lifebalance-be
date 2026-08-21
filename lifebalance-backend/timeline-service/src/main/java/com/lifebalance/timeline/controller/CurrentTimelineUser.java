package com.lifebalance.timeline.controller;

import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

final class CurrentTimelineUser {

    private CurrentTimelineUser() {
    }

    static UUID ownerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
