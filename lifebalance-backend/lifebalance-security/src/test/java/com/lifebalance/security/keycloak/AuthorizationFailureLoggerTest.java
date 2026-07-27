package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.common.error.AuthErrorCode;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(OutputCaptureExtension.class)
class AuthorizationFailureLoggerTest {

    private final AuthorizationFailureLogger logger = new AuthorizationFailureLogger();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLogAuthorizationFailureWithUserAndRequestContext(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin/users/123");
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Request-ID", "request-2");
        request.setAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                new KeycloakUserPrincipal(
                        "kc-user-1",
                        "alice",
                        "alice@example.com",
                        "Alice Nguyen",
                        "Alice",
                        "Nguyen",
                        "lifebalance-api",
                        Set.of("lifebalance-api"),
                        Set.of("user"),
                        Set.of("profile:read"),
                        Set.of("user", "profile:read")
                )
        );

        logger.logFailure(
                request,
                new AccessDeniedException("Missing required permission: user:delete"),
                AuthErrorCode.FORBIDDEN
        );

        assertThat(output.getOut())
                .contains("event=authorization_failure")
                .contains("error_code=" + AuthErrorCode.FORBIDDEN)
                .contains("user_id=kc-user-1")
                .contains("username=alice")
                .contains("method=DELETE")
                .contains("path=/api/admin/users/123")
                .contains("remote_ip=203.0.113.10")
                .contains("forwarded_for=\"198.51.100.7\"")
                .contains("request_id=\"request-2\"")
                .contains("reason=\"Missing required permission: user:delete\"");
    }
}
