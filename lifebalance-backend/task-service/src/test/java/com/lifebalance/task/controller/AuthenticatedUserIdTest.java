package com.lifebalance.task.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;

class AuthenticatedUserIdTest {

    @Test
    void returnsInternalUserIdFromMappedPrincipal() {
        UUID userId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                principal(userId));

        assertThat(AuthenticatedUserId.from(request)).isEqualTo(userId);
    }

    @Test
    void rejectsRequestWithoutMappedPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> AuthenticatedUserId.from(request))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("Authenticated internal user id is required.");
    }

    private KeycloakUserPrincipal principal(UUID userId) {
        return new KeycloakUserPrincipal(
                "keycloak-user",
                userId,
                "user",
                "user@example.com",
                "LifeBalance User",
                "LifeBalance",
                "User",
                "lifebalance-web",
                Set.of("lifebalance-api"),
                Set.of("user"),
                Set.of(),
                Set.of("user"));
    }
}
