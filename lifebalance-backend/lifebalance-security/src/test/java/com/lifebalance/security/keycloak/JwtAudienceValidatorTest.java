package com.lifebalance.security.keycloak;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAudienceValidatorTest {

    @Test
    void shouldAcceptTokenWithRequiredAudience() {
        var result = JwtAudienceValidator.requireAudience("lifebalance-api")
                .validate(jwt(List.of("account", "lifebalance-api")));

        assertFalse(result.hasErrors());
    }

    @Test
    void shouldRejectTokenWithoutRequiredAudience() {
        var result = JwtAudienceValidator.requireAudience("lifebalance-api")
                .validate(jwt(List.of("account")));

        assertTrue(result.hasErrors());
    }

    @Test
    void shouldRequireConfiguredAudience() {
        assertThrows(IllegalStateException.class, () -> JwtAudienceValidator.requireAudience(" "));
    }

    private static Jwt jwt(List<String> audience) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "user",
                        "aud", audience
                )
        );
    }
}
