package com.lifebalance.identity.config;

import java.time.Clock;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.service.CurrentPasswordVerifier;
import com.lifebalance.identity.service.PasswordChangeAttemptLimiter;
import com.lifebalance.identity.service.PasswordChangeService;
import com.lifebalance.identity.service.PasswordCredentialUpdater;
import com.lifebalance.identity.service.impl.InMemoryPasswordChangeAttemptLimiter;
import com.lifebalance.identity.service.impl.KeycloakCurrentPasswordVerifier;
import com.lifebalance.identity.service.impl.KeycloakPasswordCredentialUpdater;
import com.lifebalance.identity.service.impl.PasswordChangeServiceImpl;
import com.lifebalance.identity.service.impl.UnavailablePasswordChangeService;

@Configuration
@EnableConfigurationProperties(PasswordChangeProperties.class)
public class PasswordChangeConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.password-change",
            name = "enabled",
            havingValue = "true"
    )
    CurrentPasswordVerifier currentPasswordVerifier(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            PasswordChangeProperties properties
    ) {
        properties.validate();
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(properties.getServerUrl())
                .build();
        return new KeycloakCurrentPasswordVerifier(restClient, objectMapper, properties);
    }

    @Bean(name = "passwordChangeAdminKeycloakClient", destroyMethod = "close")
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.password-change",
            name = "enabled",
            havingValue = "true"
    )
    Keycloak passwordChangeAdminKeycloakClient(PasswordChangeProperties properties) {
        properties.validate();
        return KeycloakBuilder.builder()
                .serverUrl(properties.getServerUrl())
                .realm(properties.getAdminAuthRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(properties.getAdminClientId())
                .clientSecret(properties.getAdminClientSecret())
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.password-change",
            name = "enabled",
            havingValue = "true"
    )
    PasswordCredentialUpdater passwordCredentialUpdater(
            @Qualifier("passwordChangeAdminKeycloakClient") Keycloak keycloak,
            PasswordChangeProperties properties
    ) {
        return new KeycloakPasswordCredentialUpdater(keycloak, properties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.password-change",
            name = "enabled",
            havingValue = "true"
    )
    PasswordChangeAttemptLimiter passwordChangeAttemptLimiter(PasswordChangeProperties properties) {
        return new InMemoryPasswordChangeAttemptLimiter(properties, Clock.systemUTC());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.password-change",
            name = "enabled",
            havingValue = "true"
    )
    PasswordChangeService passwordChangeService(
            CurrentPasswordVerifier currentPasswordVerifier,
            PasswordCredentialUpdater passwordCredentialUpdater,
            PasswordChangeAttemptLimiter passwordChangeAttemptLimiter,
            PasswordChangeProperties properties
    ) {
        return new PasswordChangeServiceImpl(
                currentPasswordVerifier,
                passwordCredentialUpdater,
                passwordChangeAttemptLimiter,
                properties
        );
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.password-change",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    PasswordChangeService unavailablePasswordChangeService() {
        return new UnavailablePasswordChangeService();
    }
}
