package com.lifebalance.identity.model;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identity service health response")
public record HealthResponse(
        @Schema(description = "Overall service health", example = "ok")
        String status,
        @Schema(description = "Health check timestamp", example = "2026-07-27T03:15:30Z")
        Instant timestamp,
        @Schema(description = "Application uptime in milliseconds", example = "180000")
        long uptime,
        @Schema(description = "Service name", example = "identity-service")
        String serviceName,
        @Schema(description = "Active runtime environment", example = "local")
        String environment,
        @Schema(description = "Service version", example = "1.0.0")
        String version,
        @Schema(description = "Individual dependency checks", example = "{\"database\":\"ok\"}")
        Map<String, String> checks
) {
}
