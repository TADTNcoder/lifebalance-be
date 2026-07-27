package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.config.KeycloakRoleSyncProperties;
import com.lifebalance.identity.exception.KeycloakSessionRevocationException;
import com.lifebalance.identity.model.User;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class KeycloakUserSessionRevocationServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    private KeycloakUserSessionRevocationService service;

    @BeforeEach
    void setUp() {
        KeycloakRoleSyncProperties properties = new KeycloakRoleSyncProperties();
        properties.setRealm("lifebalance");
        service = new KeycloakUserSessionRevocationService(keycloak, properties);
    }

    @Test
    void shouldLogoutKeycloakUserSessions() {
        User user = user("kc-user-1");
        when(keycloak.realm("lifebalance")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-user-1")).thenReturn(userResource);

        service.revokeSessions(user, "USER_LOCKED");

        verify(userResource).logout();
    }

    @Test
    void shouldSkipWhenKeycloakIdIsMissing() {
        service.revokeSessions(user(null), "USER_LOCKED");

        verify(keycloak, never()).realm("lifebalance");
    }

    @Test
    void shouldIgnoreMissingKeycloakUser() {
        User user = user("kc-user-1");
        when(keycloak.realm("lifebalance")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-user-1")).thenReturn(userResource);
        doThrow(new NotFoundException()).when(userResource).logout();

        service.revokeSessions(user, "USER_DELETED");

        verify(userResource).logout();
    }

    @Test
    void shouldFailWhenKeycloakReturnsUnexpectedError() {
        User user = user("kc-user-1");
        when(keycloak.realm("lifebalance")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-user-1")).thenReturn(userResource);
        doThrow(new WebApplicationException(Response.serverError().build()))
                .when(userResource)
                .logout();

        assertThatThrownBy(() -> service.revokeSessions(user, "USER_DISABLED"))
                .isInstanceOf(KeycloakSessionRevocationException.class)
                .hasMessage("Failed to revoke Keycloak sessions for user " + user.getId() + ": HTTP 500");
    }

    private static User user(String keycloakId) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setKeycloakId(keycloakId);
        user.setEmail("alice@example.com");
        user.setStatus(com.lifebalance.identity.model.enums.AccountStatus.ACTIVE);
        return user;
    }
}
