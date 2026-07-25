package com.lifebalance.security.keycloak;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AuthorizationFailureLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("com.lifebalance.security.audit");
    private static final int MAX_VALUE_LENGTH = 256;

    public void logFailure(
            HttpServletRequest request,
            AccessDeniedException accessDeniedException,
            String errorCode
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserIdentity userIdentity = resolveUserIdentity(request, authentication);

        auditLog.warn(
                "event=authorization_failure error_code={} user_id={} username={} method={} path={} "
                        + "remote_ip={} forwarded_for=\"{}\" user_agent=\"{}\" request_id=\"{}\" "
                        + "exception={} reason=\"{}\"",
                sanitize(errorCode),
                sanitize(userIdentity.userId()),
                sanitize(userIdentity.username()),
                sanitize(request.getMethod()),
                sanitize(request.getRequestURI()),
                sanitize(request.getRemoteAddr()),
                sanitize(header(request, "X-Forwarded-For")),
                sanitize(header(request, "User-Agent")),
                sanitize(requestId(request)),
                sanitize(accessDeniedException.getClass().getSimpleName()),
                sanitize(accessDeniedException.getMessage())
        );
    }

    private UserIdentity resolveUserIdentity(
            HttpServletRequest request,
            Authentication authentication
    ) {
        Object currentUser = request.getAttribute(KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);
        if (currentUser instanceof KeycloakUserPrincipal principal) {
            return new UserIdentity(principal.subject(), principal.username());
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            return new UserIdentity(jwt.getSubject(), username(jwt, authentication));
        }

        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                return new UserIdentity(jwt.getSubject(), username(jwt, authentication));
            }

            return new UserIdentity(authentication.getName(), authentication.getName());
        }

        return new UserIdentity(null, null);
    }

    private String username(Jwt jwt, Authentication authentication) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) {
            return username;
        }

        return authentication.getName();
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

    private record UserIdentity(String userId, String username) {
    }
}
