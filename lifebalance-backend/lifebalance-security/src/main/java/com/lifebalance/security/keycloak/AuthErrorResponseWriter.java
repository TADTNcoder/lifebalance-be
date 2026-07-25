package com.lifebalance.security.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.api.ApiError;
import com.lifebalance.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AuthErrorResponseWriter {

    private final ObjectMapper objectMapper;

    AuthErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void write(
            HttpServletResponse response,
            HttpStatus status,
            ApiError error
    ) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(error));
    }
}
