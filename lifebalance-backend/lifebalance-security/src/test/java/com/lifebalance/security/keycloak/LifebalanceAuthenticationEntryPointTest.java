package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.lifebalance.common.error.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class LifebalanceAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private final LifebalanceAuthenticationEntryPoint entryPoint =
            new LifebalanceAuthenticationEntryPoint(objectMapper);

    @Test
    void shouldWriteStandardUnauthorizedJsonResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest("GET", "/api/tasks"),
                response,
                new InsufficientAuthenticationException("Full authentication is required")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("data").isNull()).isTrue();
        assertThat(body.at("/error/code").asText()).isEqualTo(AuthErrorCode.UNAUTHORIZED);
        assertThat(body.at("/error/message").asText()).isEqualTo("Authentication is required");
        assertThat(body.at("/error/details").isEmpty()).isTrue();
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }
}
