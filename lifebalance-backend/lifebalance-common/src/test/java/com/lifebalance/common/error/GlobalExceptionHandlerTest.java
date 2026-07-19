package com.lifebalance.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
}
