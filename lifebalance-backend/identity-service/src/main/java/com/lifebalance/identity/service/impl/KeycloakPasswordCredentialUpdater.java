package com.lifebalance.identity.service.impl;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;

import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.error.PasswordChangeExceptions;
import com.lifebalance.identity.service.PasswordCredentialUpdater;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeycloakPasswordCredentialUpdater implements PasswordCredentialUpdater {

    private final Keycloak keycloak;
    private final PasswordChangeProperties properties;

    public KeycloakPasswordCredentialUpdater(Keycloak keycloak, PasswordChangeProperties properties) {
        this.keycloak = keycloak;
        this.properties = properties;
    }

    @Override
    public boolean updatePasswordAndRevokeSessions(String keycloakUserId, String newPassword) {
        UserResource userResource = keycloak.realm(properties.getRealm())
                .users()
                .get(keycloakUserId);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(newPassword);

        try {
            userResource.resetPassword(credential);
        } catch (BadRequestException exception) {
            throw PasswordChangeExceptions.policyViolation(
                    "New password does not meet the Keycloak realm password policy"
            );
        } catch (NotFoundException exception) {
            throw PasswordChangeExceptions.keycloakUserNotFound();
        } catch (WebApplicationException | ProcessingException exception) {
            throw PasswordChangeExceptions.unavailable();
        } finally {
            credential.setValue(null);
        }

        try {
            userResource.logout();
            return true;
        } catch (RuntimeException exception) {
            log.warn("Password was changed, but Keycloak did not confirm user-session revocation");
            return false;
        }
    }
}
