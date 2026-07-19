package com.lifebalance.security.keycloak;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationFailureLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("com.lifebalance.security.audit");
    private static final int MAX_VALUE_LENGTH = 256;

    public void logFailure(
            HttpServletRequest request,
            AuthenticationException authException,
            String errorCode
    ) {
        auditLog.warn(
                "event=authentication_failure error_code={} method={} path={} remote_ip={} forwarded_for=\"{}\" "
                        + "user_agent=\"{}\" authorization_scheme={} request_id=\"{}\" exception={} reason=\"{}\"",
                sanitize(errorCode),
                sanitize(request.getMethod()),
                sanitize(request.getRequestURI()),
                sanitize(request.getRemoteAddr()),
                sanitize(header(request, "X-Forwarded-For")),
                sanitize(header(request, "User-Agent")),
                sanitize(authorizationScheme(request)),
                sanitize(requestId(request)),
                sanitize(authException.getClass().getSimpleName()),
                sanitize(authException.getMessage())
        );
    }

    private String header(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        return requestId;
    }

    private String authorizationScheme(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return "none";
        }

        int separator = authorization.indexOf(' ');
        if (separator <= 0) {
            return "unknown";
        }

        return authorization.substring(0, separator);
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        String sanitized = value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .replace('"', '\'')
                .trim();

        if (sanitized.length() > MAX_VALUE_LENGTH) {
            return sanitized.substring(0, MAX_VALUE_LENGTH);
        }

        return sanitized;
    }
}
