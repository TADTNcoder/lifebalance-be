package com.lifebalance.task.controller;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;

import jakarta.servlet.http.HttpServletRequest;

final class AuthenticatedUserId {

    private static final String MISSING_USER_MESSAGE =
            "Authenticated internal user id is required.";

    private AuthenticatedUserId() {
    }

    static UUID from(HttpServletRequest request) {
        KeycloakUserPrincipal currentUser = (KeycloakUserPrincipal) request.getAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);

        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException(MISSING_USER_MESSAGE);
        }

        return currentUser.userId();
    }
}
