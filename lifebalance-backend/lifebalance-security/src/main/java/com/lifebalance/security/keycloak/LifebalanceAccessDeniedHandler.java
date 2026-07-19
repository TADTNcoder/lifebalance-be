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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class LifebalanceAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public LifebalanceAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        ApiError error = ApiError.of(
                AuthErrorCode.FORBIDDEN,
                "Access is denied"
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(error));
    }
}
