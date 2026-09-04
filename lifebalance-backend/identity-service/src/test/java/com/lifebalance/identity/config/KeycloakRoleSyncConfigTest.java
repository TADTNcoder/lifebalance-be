package com.lifebalance.identity.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.lifebalance.identity.service.UserSessionRevocationService;
import com.lifebalance.identity.service.impl.KeycloakUserSessionRevocationService;
import com.lifebalance.identity.service.impl.NoopUserSessionRevocationService;

class KeycloakRoleSyncConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KeycloakRoleSyncConfig.class)
            .withPropertyValues(
                    "lifebalance.keycloak.role-sync.server-url=http://localhost:8088",
                    "lifebalance.keycloak.role-sync.realm=lifebalance",
                    "lifebalance.keycloak.role-sync.auth-realm=master",
                    "lifebalance.keycloak.role-sync.client-id=admin-cli",
                    "lifebalance.keycloak.role-sync.client-secret=test-secret"
            );

    @Test
    void shouldCreateKeycloakClientWhenOnlySessionRevocationIsEnabled() {
        contextRunner
                .withPropertyValues(
                        "lifebalance.keycloak.role-sync.enabled=false",
                        "lifebalance.keycloak.session-revocation.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(Keycloak.class);
                    assertThat(context).hasSingleBean(UserSessionRevocationService.class);
                    assertThat(context.getBean(UserSessionRevocationService.class))
                            .isInstanceOf(KeycloakUserSessionRevocationService.class);
                });
    }

    @Test
    void shouldUseNoopRevocationWhenSessionRevocationIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "lifebalance.keycloak.role-sync.enabled=false",
                        "lifebalance.keycloak.session-revocation.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(Keycloak.class);
                    assertThat(context).hasSingleBean(UserSessionRevocationService.class);
                    assertThat(context.getBean(UserSessionRevocationService.class))
                            .isInstanceOf(NoopUserSessionRevocationService.class);
                });
    }
}
