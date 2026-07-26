package com.lifebalance.identity.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentAuditRequestMetadataResolver {

    public AuditRequestMetadata resolve() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return new AuditRequestMetadata("unknown", "unknown");
        }

        return new AuditRequestMetadata(
                resolveIpAddress(request),
                valueOrUnknown(request.getHeader("User-Agent"))
        );
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }

        return null;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return valueOrUnknown(forwardedFor.split(",")[0]);
        }

        return valueOrUnknown(request.getRemoteAddr());
    }

    private String valueOrUnknown(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value.trim();
    }
}
