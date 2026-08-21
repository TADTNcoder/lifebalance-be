package com.lifebalance.task.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class RestTaskIntegrationClient implements TaskIntegrationClient {

    private static final Logger log = LoggerFactory.getLogger(RestTaskIntegrationClient.class);
    private static final String INTERNAL_SECRET_HEADER = "X-Lifebalance-Internal-Secret";

    private final RestClient.Builder restClientBuilder;
    private final TaskIntegrationProperties properties;

    RestTaskIntegrationClient(
            RestClient.Builder restClientBuilder,
            TaskIntegrationProperties properties
    ) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
    }

    @Override
    public void syncTimelineTask(TaskIntegrationEvent event) {
        if (!hasBearer(event.authorizationHeader())) {
            log.warn("Skipping timeline task sync because bearer token is missing. taskId={} action={}",
                    event.taskId(), event.action());
            return;
        }
        if (!properties.hasInternalSecret()) {
            log.warn("Skipping timeline task sync because internal service secret is not configured. taskId={} action={}",
                    event.taskId(), event.action());
            return;
        }

        TimelineTaskSyncRequest request = new TimelineTaskSyncRequest(
                event.taskId(),
                event.title(),
                timelineStatus(event),
                hasTimeCapital(event),
                event.estimatedMinutes(),
                event.deadline(),
                null,
                null,
                null,
                event.scheduledStartAt(),
                event.scheduledEndAt()
        );
        post(
                properties.getTimelineService().getBaseUrl(),
                "/api/timeline/tasks",
                request,
                event.authorizationHeader(),
                true,
                "timeline-service",
                event
        );
    }

    @Override
    public void createNotification(TaskNotificationRequest request, String authorizationHeader) {
        if (!hasBearer(authorizationHeader)) {
            log.warn("Skipping notification sync because bearer token is missing. referenceId={} eventType={}",
                    request.referenceId(), request.eventType());
            return;
        }

        post(
                properties.getNotificationService().getBaseUrl(),
                "/api/notifications",
                request,
                authorizationHeader,
                true,
                "notification-service",
                null
        );
    }

    @Override
    public void recordActualSeed(TaskActualRecordRequest request, String authorizationHeader) {
        if (!hasBearer(authorizationHeader)) {
            log.warn("Skipping analytics actual seed because bearer token is missing. taskId={}", request.taskId());
            return;
        }

        post(
                properties.getAnalyticsService().getBaseUrl(),
                "/api/analytics/actual-records",
                request,
                authorizationHeader,
                true,
                "analytics-service",
                null
        );
    }

    private void post(
            String baseUrl,
            String uri,
            Object request,
            String authorizationHeader,
            boolean includeInternalCredential,
            String serviceName,
            TaskIntegrationEvent event
    ) {
        try {
            restClientBuilder
                    .clone()
                    .baseUrl(normalizeBaseUrl(baseUrl))
                    .build()
                    .post()
                    .uri(uri)
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                        if (includeInternalCredential && properties.hasInternalSecret()) {
                            headers.set(INTERNAL_SECRET_HEADER, properties.getInternalSecret().trim());
                        }
                    })
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            if (event == null) {
                log.warn("Cross-service integration call failed. service={} uri={}", serviceName, uri, exception);
                return;
            }
            log.warn("Cross-service integration call failed. service={} uri={} taskId={} action={}",
                    serviceName, uri, event.taskId(), event.action(), exception);
        }
    }

    private static boolean hasBearer(String authorizationHeader) {
        return authorizationHeader != null
                && authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    private static boolean hasTimeCapital(TaskIntegrationEvent event) {
        return event.estimatedMinutes() != null && event.estimatedMinutes() > 0;
    }

    private static String timelineStatus(TaskIntegrationEvent event) {
        if (event.action() == TaskIntegrationAction.TASK_DELETED) {
            return "ARCHIVED";
        }
        return event.taskStatus().name();
    }

    private static String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost";
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
