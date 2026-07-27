package com.lifebalance.identity.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lifebalance.identity.service.RoleSyncService;
import com.lifebalance.identity.service.UserSessionRevocationService;
import com.lifebalance.identity.service.impl.KeycloakRoleSyncService;
import com.lifebalance.identity.service.impl.KeycloakUserSessionRevocationService;
import com.lifebalance.identity.service.impl.NoopRoleSyncService;
import com.lifebalance.identity.service.impl.NoopUserSessionRevocationService;

@Configuration
@EnableConfigurationProperties(KeycloakRoleSyncProperties.class)
public class KeycloakRoleSyncConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.role-sync",
            name = "enabled",
            havingValue = "true"
    )
    Keycloak roleSyncKeycloakClient(KeycloakRoleSyncProperties properties) {
        properties.validate();

        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(properties.getServerUrl())
                .realm(properties.getAuthRealm())
                .clientId(properties.getClientId());

        if (hasText(properties.getClientSecret())) {
            return builder
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientSecret(properties.getClientSecret())
                    .build();
        }

        return builder
                .grantType(OAuth2Constants.PASSWORD)
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.role-sync",
            name = "enabled",
            havingValue = "true"
    )
    RoleSyncService keycloakRoleSyncService(
            Keycloak roleSyncKeycloakClient,
            KeycloakRoleSyncProperties properties
    ) {
        return new KeycloakRoleSyncService(roleSyncKeycloakClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean(RoleSyncService.class)
    RoleSyncService noopRoleSyncService() {
        return new NoopRoleSyncService();
    }

    @Bean
    @ConditionalOnBean(Keycloak.class)
    @ConditionalOnProperty(
            prefix = "lifebalance.keycloak.session-revocation",
            name = "enabled",
            havingValue = "true"
    )
    UserSessionRevocationService keycloakUserSessionRevocationService(
            Keycloak roleSyncKeycloakClient,
            KeycloakRoleSyncProperties properties
    ) {
        return new KeycloakUserSessionRevocationService(roleSyncKeycloakClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean(UserSessionRevocationService.class)
    UserSessionRevocationService noopUserSessionRevocationService() {
        return new NoopUserSessionRevocationService();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
