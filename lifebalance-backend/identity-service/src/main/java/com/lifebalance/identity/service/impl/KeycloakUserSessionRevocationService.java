package com.lifebalance.identity.service.impl;

import org.keycloak.admin.client.Keycloak;

import com.lifebalance.identity.config.KeycloakRoleSyncProperties;
import com.lifebalance.identity.exception.KeycloakSessionRevocationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.UserSessionRevocationService;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSessionRevocationService implements UserSessionRevocationService {

    private final Keycloak keycloak;
    private final KeycloakRoleSyncProperties properties;

    @Override
    public void revokeSessions(User user, String reason) {
        String keycloakId = keycloakId(user);
        if (keycloakId == null) {
            log.info("Skipping Keycloak session revocation for user {} because keycloak id is missing", userId(user));
            return;
        }

        try {
            keycloak.realm(properties.getRealm())
                    .users()
                    .get(keycloakId)
                    .logout();
            log.info(
                    "Revoked Keycloak sessions for user {} with reason '{}'",
                    userId(user),
                    safeReason(reason)
            );
        } catch (NotFoundException exception) {
            log.info(
                    "Skipping Keycloak session revocation for user {} because Keycloak user {} was not found",
                    userId(user),
                    keycloakId
            );
        } catch (WebApplicationException exception) {
            throw failure(user, exception);
        } catch (ProcessingException exception) {
            throw failure(user, exception);
        }
    }

    private static KeycloakSessionRevocationException failure(User user, RuntimeException exception) {
        int status = exception instanceof WebApplicationException webApplicationException
                ? status(webApplicationException)
                : -1;
        String statusText = status > 0 ? "HTTP " + status : exception.getClass().getSimpleName();

        return new KeycloakSessionRevocationException(
                "Failed to revoke Keycloak sessions for user " + userId(user) + ": " + statusText
        );
    }

    private static int status(WebApplicationException exception) {
        Response response = exception.getResponse();
        return response == null ? -1 : response.getStatus();
    }

    private static String keycloakId(User user) {
        return user == null ? null : trimToNull(user.getKeycloakId());
    }

    private static String userId(User user) {
        return user == null || user.getId() == null ? "unknown" : user.getId().toString();
    }

    private static String safeReason(String reason) {
        String normalized = trimToNull(reason);
        return normalized == null ? "unspecified" : normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
