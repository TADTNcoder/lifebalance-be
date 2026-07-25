package com.lifebalance.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SecurityExceptionAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("com.lifebalance.security.audit");
    private static final int MAX_VALUE_LENGTH = 256;

    void logAuthenticationFailure(AuthenticationException exception, String errorCode) {
        HttpServletRequest request = currentRequest();
        auditLog.warn(
                "event=authentication_failure error_code={} method={} path={} remote_ip={} "
                        + "forwarded_for=\"{}\" user_agent=\"{}\" request_id=\"{}\" exception={} reason=\"{}\"",
                sanitize(errorCode),
                sanitize(method(request)),
                sanitize(path(request)),
                sanitize(remoteAddress(request)),
                sanitize(header(request, "X-Forwarded-For")),
                sanitize(header(request, "User-Agent")),
                sanitize(requestId(request)),
                sanitize(exception.getClass().getSimpleName()),
                sanitize(exception.getMessage())
        );
    }

    void logAuthorizationFailure(AccessDeniedException exception, String errorCode) {
        HttpServletRequest request = currentRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        auditLog.warn(
                "event=authorization_failure error_code={} user_id={} username={} method={} path={} "
                        + "remote_ip={} forwarded_for=\"{}\" user_agent=\"{}\" request_id=\"{}\" "
                        + "exception={} reason=\"{}\"",
                sanitize(errorCode),
                sanitize(userId(authentication)),
                sanitize(username(authentication)),
                sanitize(method(request)),
                sanitize(path(request)),
                sanitize(remoteAddress(request)),
                sanitize(header(request, "X-Forwarded-For")),
                sanitize(header(request, "User-Agent")),
                sanitize(requestId(request)),
                sanitize(exception.getClass().getSimpleName()),
                sanitize(exception.getMessage())
        );
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }

        return null;
    }

    private String method(HttpServletRequest request) {
        return request == null ? null : request.getMethod();
    }

    private String path(HttpServletRequest request) {
        return request == null ? null : request.getRequestURI();
    }

    private String remoteAddress(HttpServletRequest request) {
        return request == null ? null : request.getRemoteAddr();
    }

    private String header(HttpServletRequest request, String name) {
        return request == null ? null : request.getHeader(name);
    }

    private String requestId(HttpServletRequest request) {
        String requestId = header(request, "X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = header(request, "X-Correlation-ID");
        }
        return requestId;
    }

    private String userId(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    private String username(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
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
