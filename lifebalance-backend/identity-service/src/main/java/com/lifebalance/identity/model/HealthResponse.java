package com.lifebalance.identity.model;

import java.time.Instant;
import java.util.Map;

public record HealthResponse(
        String status,
        Instant timestamp,
        long uptime,
        String serviceName,
        String environment,
        String version,
        Map<String, String> checks
) {
}