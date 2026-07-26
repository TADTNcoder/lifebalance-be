package com.lifebalance.identity.audit;

public record AuditRequestMetadata(
        String ipAddress,
        String userAgent
) {
}
