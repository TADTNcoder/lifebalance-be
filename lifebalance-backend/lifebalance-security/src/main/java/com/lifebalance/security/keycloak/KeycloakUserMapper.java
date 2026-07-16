package com.lifebalance.security.keycloak;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakUserMapper {

    private static final String CLAIM_SUBJECT = "sub";
    private static final String CLAIM_USERNAME = "preferred_username";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_GIVEN_NAME = "given_name";
    private static final String CLAIM_FAMILY_NAME = "family_name";
    private static final String CLAIM_AUTHORIZED_PARTY = "azp";
    private static final String CLAIM_AUDIENCE = "aud";
    private static final String CLAIM_REALM_ACCESS = "realm_access";
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String CLAIM_ROLES = "roles";

    private final KeycloakSecurityProperties properties;

    public KeycloakUserMapper(KeycloakSecurityProperties properties) {
        this.properties = properties;
    }

    public KeycloakUserPrincipal map(Jwt jwt) {
        Set<String> realmRoles = extractRealmRoles(jwt);
        Set<String> clientRoles = extractClientRoles(jwt, properties.getClientId());

        Set<String> roles = new LinkedHashSet<>();
        roles.addAll(realmRoles);
        roles.addAll(clientRoles);

        return new KeycloakUserPrincipal(
                stringClaim(jwt, CLAIM_SUBJECT),
                stringClaim(jwt, CLAIM_USERNAME),
                stringClaim(jwt, CLAIM_EMAIL),
                stringClaim(jwt, CLAIM_NAME),
                stringClaim(jwt, CLAIM_GIVEN_NAME),
                stringClaim(jwt, CLAIM_FAMILY_NAME),
                stringClaim(jwt, CLAIM_AUTHORIZED_PARTY),
                extractAudiences(jwt),
                realmRoles,
                clientRoles,
                roles
        );
    }

    private Set<String> extractRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get(CLAIM_REALM_ACCESS);
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return Set.of();
        }

        return stringSet(realmAccessMap.get(CLAIM_ROLES));
    }

    private Set<String> extractClientRoles(Jwt jwt, String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return Set.of();
        }

        Object resourceAccess = jwt.getClaims().get(CLAIM_RESOURCE_ACCESS);
        if (!(resourceAccess instanceof Map<?, ?> resourceAccessMap)) {
            return Set.of();
        }

        Object clientAccess = resourceAccessMap.get(clientId);
        if (!(clientAccess instanceof Map<?, ?> clientAccessMap)) {
            return Set.of();
        }

        return stringSet(clientAccessMap.get(CLAIM_ROLES));
    }

    private Set<String> extractAudiences(Jwt jwt) {
        Object audiences = jwt.getClaims().get(CLAIM_AUDIENCE);

        if (audiences instanceof String singleAudience && !singleAudience.isBlank()) {
            return Set.of(singleAudience);
        }

        return stringSet(audiences);
    }

    private String stringClaim(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(claimName);
        if (value instanceof String stringValue) {
            return stringValue;
        }

        return null;
    }

    private Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }

        Set<String> result = new LinkedHashSet<>();
        for (Object item : collection) {
            if (item instanceof String stringItem && !stringItem.isBlank()) {
                result.add(stringItem);
            }
        }

        return result;
    }

}
