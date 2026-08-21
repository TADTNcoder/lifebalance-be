package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class PublicReadinessBearerTokenResolverTest {

    private final PublicReadinessBearerTokenResolver resolver =
            new PublicReadinessBearerTokenResolver();

    @Test
    void shouldIgnoreBearerTokenOnPublicApiReadinessEndpoints() {
        assertThat(resolve("GET", "/api/tasks/status")).isNull();
        assertThat(resolve("GET", "/api/v1/identity/status")).isNull();
        assertThat(resolve("GET", "/api/identity/health")).isNull();
    }

    @Test
    void shouldIgnoreBearerTokenOnPublicActuatorEndpoints() {
        assertThat(resolve("GET", "/actuator/info")).isNull();
        assertThat(resolve("GET", "/actuator/prometheus")).isNull();
        assertThat(resolve("GET", "/actuator/health")).isNull();
        assertThat(resolve("GET", "/actuator/health/readiness")).isNull();
    }

    @Test
    void shouldResolveBearerTokenOnProtectedEndpoint() {
        assertThat(resolve("GET", "/api/tasks")).isEqualTo("secret-token");
    }

    @Test
    void shouldResolveBearerTokenForNonGetReadinessEndpoint() {
        assertThat(resolve("POST", "/api/tasks/status")).isEqualTo("secret-token");
    }

    private String resolve(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("Authorization", "Bearer secret-token");

        return resolver.resolve(request);
    }
}
