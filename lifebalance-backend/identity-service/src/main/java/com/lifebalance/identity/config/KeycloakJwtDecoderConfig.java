package com.lifebalance.identity.config;

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.lifebalance.security.keycloak.JwtAudienceValidator;
import com.lifebalance.security.keycloak.KeycloakSecurityProperties;

@Configuration
@EnableConfigurationProperties({KeycloakJwtProperties.class, KeycloakSecurityProperties.class})
public class KeycloakJwtDecoderConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.security.keycloak.jwt",
            name = "jwk-set-uri"
    )
    JwtDecoder keycloakJwtDecoder(
            KeycloakJwtProperties properties,
            KeycloakSecurityProperties keycloakSecurityProperties
    ) {
        properties.validate();

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(properties.getJwkSetUri())
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issuerValidator(properties.allowedIssuerSet()),
                JwtAudienceValidator.requireAudience(keycloakSecurityProperties.getClientId())
        ));

        return decoder;
    }

    private OAuth2TokenValidator<Jwt> issuerValidator(Set<String> allowedIssuers) {
        return jwt -> {
            String issuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
            if (issuer != null && allowedIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Unsupported Keycloak issuer: " + issuer,
                    null
            ));
        };
    }
}
