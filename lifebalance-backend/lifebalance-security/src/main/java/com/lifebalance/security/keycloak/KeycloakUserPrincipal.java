package com.lifebalance.security.keycloak;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record KeycloakUserPrincipal(
        String subject,
        String username,
        String email,
        String fullName,
        String givenName,
        String familyName,
        String authorizedParty,
        Set<String> audiences,
        Set<String> realmRoles,
        Set<String> clientRoles,
        Set<String> roles
) {

    public KeycloakUserPrincipal {
        audiences = immutableCopy(audiences);
        realmRoles = immutableCopy(realmRoles);
        clientRoles = immutableCopy(clientRoles);
        roles = immutableCopy(roles);
    }

    private static Set<String> immutableCopy(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

}
