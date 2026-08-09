package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakUserMapperTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void shouldMapSupportedClaims() {
        KeycloakUserMapper mapper = mapper("lifebalance-api");
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", "kc-user-1");
        claims.put("lifebalance_user_id", USER_ID.toString());
        claims.put("preferred_username", "john");
        claims.put("email", "john@example.com");
        claims.put("name", "John Doe");
        claims.put("given_name", "John");
        claims.put("family_name", "Doe");
        claims.put("azp", "lifebalance-api");
        claims.put("aud", List.of("account", "lifebalance-api"));
        claims.put("realm_access", Map.of("roles", List.of("admin", "user")));
        claims.put(
                "resource_access",
                Map.of("lifebalance-api", Map.of("roles", List.of("task:read", "task:write")))
        );
        Jwt jwt = jwt(claims);

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.subject()).isEqualTo("kc-user-1");
        assertThat(user.userId()).isEqualTo(USER_ID);
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

    @Test
    void shouldIgnoreMalformedInternalUserIdClaim() {
        KeycloakUserMapper mapper = mapper("lifebalance-api");
        Jwt jwt = jwt(Map.of("lifebalance_user_id", "not-a-uuid"));

        KeycloakUserPrincipal user = mapper.map(jwt);

        assertThat(user.userId()).isNull();
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
