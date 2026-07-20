package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.lifebalance.common.error.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class LifebalanceAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private final LifebalanceAccessDeniedHandler accessDeniedHandler =
            new LifebalanceAccessDeniedHandler(objectMapper);

    @Test
    void shouldWriteStandardForbiddenJsonResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                new MockHttpServletRequest("GET", "/api/admin"),
                response,
                new AccessDeniedException("not allowed")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("data").isNull()).isTrue();
        assertThat(body.at("/error/code").asText()).isEqualTo(AuthErrorCode.FORBIDDEN);
        assertThat(body.at("/error/message").asText()).isEqualTo("Access is denied");
        assertThat(body.at("/error/details").isEmpty()).isTrue();
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }
}
