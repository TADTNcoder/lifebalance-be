package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.lifebalance.common.error.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class LifebalanceAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    void shouldWriteStandardForbiddenJsonResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingAuthorizationFailureLogger logger = new RecordingAuthorizationFailureLogger();
        LifebalanceAccessDeniedHandler accessDeniedHandler =
                new LifebalanceAccessDeniedHandler(objectMapper, logger);

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
        assertThat(logger.errorCode).isEqualTo(AuthErrorCode.FORBIDDEN);
        assertThat(logger.path).isEqualTo("/api/admin");
    }

    private static class RecordingAuthorizationFailureLogger extends AuthorizationFailureLogger {

        private String errorCode;
        private String path;

        @Override
        public void logFailure(
                HttpServletRequest request,
                AccessDeniedException accessDeniedException,
                String errorCode
        ) {
            this.errorCode = errorCode;
            this.path = request.getRequestURI();
        }
    }
}
