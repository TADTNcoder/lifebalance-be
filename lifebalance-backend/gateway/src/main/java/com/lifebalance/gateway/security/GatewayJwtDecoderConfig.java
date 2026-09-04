package com.lifebalance.gateway.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestClient;

import com.lifebalance.security.keycloak.JwtAudienceValidator;
import com.lifebalance.security.keycloak.KeycloakSecurityProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AccountStatusValidationProperties.class)
public class GatewayJwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(
            OAuth2ResourceServerProperties resourceServerProperties,
            KeycloakSecurityProperties keycloakSecurityProperties,
            AccountStatusValidationProperties accountStatusProperties,
            RestClient.Builder restClientBuilder
    ) {
        String issuerUri = resourceServerProperties.getJwt().getIssuerUri();
        String jwkSetUri = resourceServerProperties.getJwt().getJwkSetUri();
        if ((issuerUri == null || issuerUri.isBlank()) && (jwkSetUri == null || jwkSetUri.isBlank())) {
            throw new IllegalStateException("JWT issuer-uri or jwk-set-uri is required");
        }

        NimbusJwtDecoder decoder = jwkSetUri == null || jwkSetUri.isBlank()
                ? NimbusJwtDecoder.withIssuerLocation(issuerUri).build()
                : NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(issuerUri == null || issuerUri.isBlank()
                ? new JwtTimestampValidator()
                : JwtValidators.createDefaultWithIssuer(issuerUri));
        validators.add(JwtAudienceValidator.requireAudience(keycloakSecurityProperties.getClientId()));

        if (accountStatusProperties.isEnabled()) {
            validators.add(new IdentityAccountStatusTokenValidator(
                    restClientBuilder.build(),
                    accountStatusProperties.getUrl()
            ));
        }

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
