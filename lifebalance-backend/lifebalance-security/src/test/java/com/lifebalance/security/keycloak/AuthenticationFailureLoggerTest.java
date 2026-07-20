package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.common.error.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.InsufficientAuthenticationException;

@ExtendWith(OutputCaptureExtension.class)
class AuthenticationFailureLoggerTest {

    private final AuthenticationFailureLogger logger = new AuthenticationFailureLogger();

    @Test
    void shouldLogAuthenticationFailureWithoutAuthorizationSecret(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tasks");
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("Authorization", "Bearer secret-token-value");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Request-ID", "request-1");

        logger.logFailure(
                request,
                new InsufficientAuthenticationException("Full authentication is required"),
                AuthErrorCode.UNAUTHORIZED
        );

        assertThat(output.getOut())
                .contains("event=authentication_failure")
                .contains("error_code=" + AuthErrorCode.UNAUTHORIZED)
                .contains("method=GET")
                .contains("path=/api/tasks")
                .contains("remote_ip=203.0.113.10")
                .contains("forwarded_for=\"198.51.100.7\"")
                .contains("authorization_scheme=Bearer")
                .contains("request_id=\"request-1\"")
                .doesNotContain("secret-token-value");
    }
}
