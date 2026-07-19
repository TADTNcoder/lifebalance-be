package com.lifebalance.security.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.api.ApiError;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.error.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class LifebalanceAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public LifebalanceAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ApiError error = ApiError.of(resolveCode(authException), resolveMessage(authException));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(error));
    }

    private String resolveCode(AuthenticationException authException) {
        if (isInvalidToken(authException)) {
            return isExpiredToken(authException)
                    ? AuthErrorCode.EXPIRED_TOKEN
                    : AuthErrorCode.INVALID_TOKEN;
        }

        return AuthErrorCode.UNAUTHORIZED;
    }

    private String resolveMessage(AuthenticationException authException) {
        if (isInvalidToken(authException)) {
            return isExpiredToken(authException)
                    ? "Access token has expired"
                    : "Access token is invalid";
        }

        return "Authentication is required";
    }

    private boolean isInvalidToken(AuthenticationException authException) {
        if (!(authException instanceof OAuth2AuthenticationException oauth2Exception)) {
            return false;
        }

        OAuth2Error error = oauth2Exception.getError();
        return error != null && OAuth2ErrorCodes.INVALID_TOKEN.equals(error.getErrorCode());
    }

    private boolean isExpiredToken(AuthenticationException authException) {
        Throwable current = authException;
        while (current != null) {
            if (containsExpiredTokenSignal(current.getMessage())) {
                return true;
            }
            current = current.getCause();
        }

        if (authException instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            return error != null && containsExpiredTokenSignal(error.getDescription());
        }

        return false;
    }

    private boolean containsExpiredTokenSignal(String message) {
        return message != null && message.toLowerCase().contains("expired");
    }
}
