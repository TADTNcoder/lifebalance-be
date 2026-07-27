package com.lifebalance.identity.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lifebalance.security.keycloak.jwt")
public class KeycloakJwtProperties {

    private String jwkSetUri;
    private String allowedIssuers;

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = trimToNull(jwkSetUri);
    }

    public String getAllowedIssuers() {
        return allowedIssuers;
    }

    public void setAllowedIssuers(String allowedIssuers) {
        this.allowedIssuers = trimToNull(allowedIssuers);
    }

    public Set<String> allowedIssuerSet() {
        if (allowedIssuers == null) {
            return Set.of();
        }

        return Arrays.stream(allowedIssuers.split(","))
                .map(KeycloakJwtProperties::trimToNull)
                .filter(value -> value != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void validate() {
        if (jwkSetUri == null) {
            throw new IllegalStateException("Keycloak JWK set URI is required");
        }
        if (allowedIssuerSet().isEmpty()) {
            throw new IllegalStateException("At least one Keycloak issuer is required");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
