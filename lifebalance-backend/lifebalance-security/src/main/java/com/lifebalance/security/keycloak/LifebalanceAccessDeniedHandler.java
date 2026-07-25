package com.lifebalance.security.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.api.ApiError;
import com.lifebalance.common.error.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class LifebalanceAccessDeniedHandler implements AccessDeniedHandler {

    private final AuthErrorResponseWriter responseWriter;
    private final AuthorizationFailureLogger authorizationFailureLogger;

    public LifebalanceAccessDeniedHandler(ObjectMapper objectMapper) {
        this(objectMapper, new AuthorizationFailureLogger());
    }

    public LifebalanceAccessDeniedHandler(
            ObjectMapper objectMapper,
            AuthorizationFailureLogger authorizationFailureLogger
    ) {
        this.responseWriter = new AuthErrorResponseWriter(objectMapper);
        this.authorizationFailureLogger = authorizationFailureLogger;
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
        authorizationFailureLogger.logFailure(request, accessDeniedException, error.code());

        responseWriter.write(response, HttpStatus.FORBIDDEN, error);
    }
}
