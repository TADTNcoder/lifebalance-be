package com.lifebalance.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.common.api.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldHandleAuthenticationExceptionAsStandardUnauthorizedJson() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationException(
                new InsufficientAuthenticationException("missing authentication")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error().code()).isEqualTo(AuthErrorCode.UNAUTHORIZED);
        assertThat(response.getBody().error().message()).isEqualTo("Authentication is required");
        assertThat(response.getBody().error().details()).isEmpty();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void shouldHandleAccessDeniedExceptionAsStandardForbiddenJson() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDeniedException(
                new AccessDeniedException("not allowed")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error().code()).isEqualTo(AuthErrorCode.FORBIDDEN);
        assertThat(response.getBody().error().message()).isEqualTo("Access is denied");
        assertThat(response.getBody().error().details()).isEmpty();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void shouldHandleUnreadableRequestBodyAsStandardBadRequestJson() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleMessageNotReadableException(
                new HttpMessageNotReadableException(
                        "unsupported enum value",
                        new MockHttpInputMessage(new byte[0])
                )
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error().code()).isEqualTo(CommonErrorCode.VALIDATION_FAILED);
        assertThat(response.getBody().error().message()).isEqualTo("Request body has invalid format");
        assertThat(response.getBody().error().details()).containsEntry(
                "body",
                "must be valid JSON with supported field values"
        );
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void shouldLogAccessDeniedExceptionWithMvcRequestContext(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/users");
        request.setRemoteAddr("203.0.113.11");
        request.addHeader("X-Forwarded-For", "198.51.100.8");
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Request-ID", "request-3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("kc-admin-1", "n/a")
        );

        handler.handleAccessDeniedException(new AccessDeniedException("Missing required role: admin"));

        assertThat(output.getOut())
                .contains("event=authorization_failure")
                .contains("error_code=" + AuthErrorCode.FORBIDDEN)
                .contains("user_id=kc-admin-1")
                .contains("username=kc-admin-1")
                .contains("method=POST")
                .contains("path=/api/admin/users")
                .contains("remote_ip=203.0.113.11")
                .contains("forwarded_for=\"198.51.100.8\"")
                .contains("request_id=\"request-3\"")
                .contains("reason=\"Missing required role: admin\"");
    }
}
