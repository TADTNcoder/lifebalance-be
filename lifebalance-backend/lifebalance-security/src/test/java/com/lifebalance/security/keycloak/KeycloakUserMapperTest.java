package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakUserMapperTest {

    @Test
    void shouldMapSupportedClaims() {
        KeycloakUserMapper mapper = mapper("lifebalance-api");
        Jwt jwt = jwt(Map.of(
                "sub", "user-1",
                "preferred_username", "john",
                "email", "john@example.com",
                "name", "John Doe",
                "given_name", "John",
                "family_name", "Doe",
                "azp", "lifebalance-api",
                "aud", List.of("account", "lifebalance-api"),
                "realm_access", Map.of("roles", List.of("admin", "user")),
                "resource_access", Map.of(
                        "lifebalance-api", Map.of("roles", List.of("task:read", "task:write"))
                )
        ));

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.subject()).isEqualTo("user-1");
        assertThat(user.username()).isEqualTo("john");
        assertThat(user.email()).isEqualTo("john@example.com");
        assertThat(user.fullName()).isEqualTo("John Doe");
        assertThat(user.givenName()).isEqualTo("John");
        assertThat(user.familyName()).isEqualTo("Doe");
        assertThat(user.authorizedParty()).isEqualTo("lifebalance-api");
        assertThat(user.audiences()).containsExactly("account", "lifebalance-api");
        assertThat(user.realmRoles()).containsExactly("admin", "user");
        assertThat(user.clientRoles()).containsExactly("task:read", "task:write");
        assertThat(user.roles()).containsExactly("admin", "user", "task:read", "task:write");
    }

    @Test
    void shouldMapAudienceWhenSingleString() {
        KeycloakUserMapper mapper = mapper("lifebalance-api");
        Jwt jwt = jwt(Map.of("aud", "lifebalance-api"));

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.audiences()).containsExactly("lifebalance-api");
    }

    @Test
    void shouldIgnoreMissingRoleClaims() {
        KeycloakUserMapper mapper = mapper("lifebalance-api");
        Jwt jwt = jwt(Map.of("sub", "user-1"));

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.realmRoles()).isEmpty();
        assertThat(user.clientRoles()).isEmpty();
        assertThat(user.roles()).isEmpty();
    }

    @Test
    void shouldIgnoreMalformedRoleClaims() {
        KeycloakUserMapper mapper = mapper("lifebalance-api");
        Jwt jwt = jwt(Map.of(
                "realm_access", Map.of("roles", 123),
                "resource_access", Map.of(
                        "lifebalance-api", Map.of("roles", Map.of("invalid", "value"))
                )
        ));

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.realmRoles()).isEmpty();
        assertThat(user.clientRoles()).isEmpty();
        assertThat(user.roles()).isEmpty();
    }

    @Test
    void shouldUseConfiguredClientId() {
        KeycloakUserMapper mapper = mapper("custom-client");
        Jwt jwt = jwt(Map.of(
                "resource_access", Map.of(
                        "lifebalance-api", Map.of("roles", List.of("wrong")),
                        "custom-client", Map.of("roles", List.of("right"))
                )
        ));

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.clientRoles()).containsExactly("right");
    }

    private KeycloakUserMapper mapper(String clientId) {
        KeycloakSecurityProperties properties = new KeycloakSecurityProperties();
        properties.setClientId(clientId);
        return new KeycloakUserMapper(properties);
    }

    private Jwt jwt(Map<String, Object> claims) {
        Map<String, Object> finalClaims = new LinkedHashMap<>(claims);

        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(existingClaims -> existingClaims.putAll(finalClaims))
                .build();
    }

}
