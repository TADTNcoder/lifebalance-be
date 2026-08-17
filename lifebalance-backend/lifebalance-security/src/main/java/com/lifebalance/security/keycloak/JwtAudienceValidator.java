package com.lifebalance.security.keycloak;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtAudienceValidator {

    private JwtAudienceValidator() {
    }

    public static OAuth2TokenValidator<Jwt> requireAudience(String audience) {
        String expectedAudience = normalize(audience);
        if (expectedAudience == null) {
            throw new IllegalStateException("JWT audience is required");
        }

        return jwt -> {
            if (jwt.getAudience().contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Required audience is missing: " + expectedAudience,
                    null
            ));
        };
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
