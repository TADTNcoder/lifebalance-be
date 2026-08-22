package com.lifebalance.security.keycloak;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public final class PublicReadinessBearerTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        if (isPublicReadinessRequest(request)) {
            return null;
        }

        return delegate.resolve(request);
    }

    static boolean isPublicReadinessRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = pathWithinApplication(request);
        return isPublicActuatorPath(path)
                || isPublicApiReadinessPath(path);
    }

    private static boolean isPublicActuatorPath(String path) {
        return "/actuator/info".equals(path)
                || "/actuator/prometheus".equals(path)
                || "/actuator/health".equals(path)
                || path.startsWith("/actuator/health/");
    }

    private static boolean isPublicApiReadinessPath(String path) {
        return path.startsWith("/api/")
                && (path.endsWith("/status") || path.endsWith("/health"));
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }

        return path;
    }
}
