package com.lifebalance.identity.service;

import com.lifebalance.identity.model.HealthResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@Service
public class HealthService {

    private static final String HEALTHY_STATUS = "ok";
    private static final String UNHEALTHY_STATUS = "error";
    private static final String DEFAULT_SERVICE_NAME = "identity-service";
    private static final String UNKNOWN_VERSION = "unknown";

    private final DatabaseHealthChecker databaseHealthChecker;
    private final Environment environment;

    public HealthService(
            DatabaseHealthChecker databaseHealthChecker,
            Environment environment
    ) {
        this.databaseHealthChecker = databaseHealthChecker;
        this.environment = environment;
    }

    public HealthResponse getHealth() {
        boolean databaseHealthy = databaseHealthChecker.isHealthy();

        String databaseStatus = databaseHealthy
                ? HEALTHY_STATUS
                : UNHEALTHY_STATUS;

        String overallStatus = databaseHealthy
                ? HEALTHY_STATUS
                : UNHEALTHY_STATUS;

        return new HealthResponse(
                overallStatus,
                Instant.now(),
                getUptimeSeconds(),
                getServiceName(),
                getEnvironmentName(),
                getVersion(),
                Map.of("database", databaseStatus)
        );
    }

    private long getUptimeSeconds() {
        long uptimeMilliseconds =
                ManagementFactory.getRuntimeMXBean().getUptime();

        return uptimeMilliseconds / 1_000;
    }

    private String getServiceName() {
        return environment.getProperty(
                "spring.application.name",
                DEFAULT_SERVICE_NAME
        );
    }

    private String getEnvironmentName() {
        String[] activeProfiles = environment.getActiveProfiles();

        if (activeProfiles.length > 0) {
            return String.join(",", activeProfiles);
        }

        String[] defaultProfiles = environment.getDefaultProfiles();

        if (defaultProfiles.length > 0) {
            return String.join(",", defaultProfiles);
        }

        return "default";
    }

    private String getVersion() {
        String configuredVersion =
                environment.getProperty("info.app.version");

        if (StringUtils.hasText(configuredVersion)) {
            return configuredVersion;
        }

        Package applicationPackage = HealthService.class.getPackage();

        if (applicationPackage != null
                && StringUtils.hasText(
                applicationPackage.getImplementationVersion()
        )) {
            return applicationPackage.getImplementationVersion();
        }

        return UNKNOWN_VERSION;
    }
}