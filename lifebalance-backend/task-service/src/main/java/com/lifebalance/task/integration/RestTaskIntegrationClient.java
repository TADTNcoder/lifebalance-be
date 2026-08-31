package com.lifebalance.task.integration;

import java.net.http.HttpClient;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class RestTaskIntegrationClient implements TaskIntegrationClient {

    private static final Logger log = LoggerFactory.getLogger(RestTaskIntegrationClient.class);
    private static final String INTERNAL_SECRET_HEADER = "X-Lifebalance-Internal-Secret";

    private final RestClient.Builder restClientBuilder;
    private final TaskIntegrationProperties properties;
    private final JdkClientHttpRequestFactory requestFactory;

    RestTaskIntegrationClient(
            RestClient.Builder restClientBuilder,
            TaskIntegrationProperties properties
    ) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                .build();
        this.requestFactory = new JdkClientHttpRequestFactory(httpClient);
        this.requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()));
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
                event.ownerId(),
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

    @Override
    public void recordMonthlyIncome(
            MonthlyIncomeTransactionRequest request,
            String authorizationHeader) {

        if (!hasBearer(authorizationHeader)) {
            log.warn("Skipping monthly income settlement because bearer token is missing. taskId={}",
                    request.taskId());
            return;
        }

        post(
                properties.getFinanceService().getBaseUrl(),
                "/api/transactions",
                request,
                authorizationHeader,
                true,
                "finance-service",
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
        RestClientException lastFailure = null;
        int maxAttempts = properties.getMaxAttempts();
        int attemptsMade = 0;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            attemptsMade = attempt;
            try {
                restClientBuilder
                        .clone()
                        .baseUrl(normalizeBaseUrl(baseUrl))
                        // Keep retry accounting in this class. Some auto-configured
                        // Apache clients retry 503 responses on their own, which can
                        // multiply POST attempts and duplicate side effects.
                        .requestFactory(requestFactory)
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
                return;
            } catch (RestClientException exception) {
                lastFailure = exception;
                if (!isRetryable(exception)
                        || attempt >= maxAttempts
                        || !waitBeforeRetry(attempt)) {
                    break;
                }
            }
        }

        if (event == null) {
            log.warn("Cross-service integration call failed after {} attempt(s). service={} uri={}",
                    attemptsMade, serviceName, uri, lastFailure);
        } else {
            log.warn("Cross-service integration call failed after {} attempt(s). service={} uri={} taskId={} action={}",
                    attemptsMade, serviceName, uri, event.taskId(), event.action(), lastFailure);
        }
    }

    private static boolean isRetryable(RestClientException exception) {
        if (!(exception instanceof RestClientResponseException responseException)) {
            return true;
        }
        int status = responseException.getStatusCode().value();
        return responseException.getStatusCode().is5xxServerError()
                || status == 408
                || status == 425
                || status == 429;
    }

    private boolean waitBeforeRetry(int completedAttempts) {
        long delayMillis = properties.getRetryBackoffMillis() * completedAttempts;
        if (delayMillis <= 0) {
            return true;
        }
        try {
            Thread.sleep(delayMillis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
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
