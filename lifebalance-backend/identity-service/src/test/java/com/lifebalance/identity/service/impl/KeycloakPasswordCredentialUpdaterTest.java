package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.error.IdentityErrorCode;

import jakarta.ws.rs.BadRequestException;

@ExtendWith(MockitoExtension.class)
class KeycloakPasswordCredentialUpdaterTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    private KeycloakPasswordCredentialUpdater updater;

    @BeforeEach
    void setUp() {
        PasswordChangeProperties properties = new PasswordChangeProperties();
        properties.setRealm("lifebalance");
        updater = new KeycloakPasswordCredentialUpdater(keycloak, properties);
        when(keycloak.realm("lifebalance")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-user-1")).thenReturn(userResource);
    }

    @Test
    void shouldResetPermanentCredentialAndLogoutUserSessions() {
        doAnswer(invocation -> {
            CredentialRepresentation credential = invocation.getArgument(0);
            assertThat(credential.getType()).isEqualTo(CredentialRepresentation.PASSWORD);
            assertThat(credential.isTemporary()).isFalse();
            assertThat(credential.getValue()).isEqualTo("NewPassword1!");
            return null;
        }).when(userResource).resetPassword(any(CredentialRepresentation.class));

        assertThat(updater.updatePasswordAndRevokeSessions("kc-user-1", "NewPassword1!")).isTrue();

        verify(userResource).logout();
    }

    @Test
    void shouldMapKeycloakPasswordPolicyFailureWithoutLeakingCredential() {
        doThrow(new BadRequestException()).when(userResource)
                .resetPassword(any(CredentialRepresentation.class));

        assertThatThrownBy(() -> updater.updatePasswordAndRevokeSessions("kc-user-1", "SecretValue1!"))
                .isInstanceOfSatisfying(AppException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(IdentityErrorCode.PASSWORD_POLICY_VIOLATION);
                    assertThat(exception.getMessage()).doesNotContain("SecretValue1!");
                });
    }

    @Test
    void shouldKeepSuccessfulPasswordResultWhenSessionRevocationFails() {
        doThrow(new IllegalStateException("logout unavailable")).when(userResource).logout();

        assertThat(updater.updatePasswordAndRevokeSessions("kc-user-1", "NewPassword1!")).isFalse();
    }
}
