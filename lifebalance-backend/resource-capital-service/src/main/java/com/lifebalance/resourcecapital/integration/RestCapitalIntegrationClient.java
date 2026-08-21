package com.lifebalance.resourcecapital.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class RestCapitalIntegrationClient implements CapitalIntegrationClient {

    private static final Logger log = LoggerFactory.getLogger(RestCapitalIntegrationClient.class);

    private final RestClient.Builder restClientBuilder;
    private final CapitalIntegrationProperties properties;

    RestCapitalIntegrationClient(
            RestClient.Builder restClientBuilder,
            CapitalIntegrationProperties properties
    ) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
    }

    @Override
    public void createNotification(CapitalNotificationRequest request, String authorizationHeader) {
        if (!hasBearer(authorizationHeader)) {
            log.warn("Skipping capital alert notification because bearer token is missing. referenceId={}",
                    request.referenceId());
            return;
        }
        try {
            restClientBuilder
                    .clone()
                    .baseUrl(normalizeBaseUrl(properties.getNotificationService().getBaseUrl()))
                    .build()
                    .post()
                    .uri("/api/notifications")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Capital alert notification integration failed. referenceId={}", request.referenceId(), exception);
        }
    }

    private static boolean hasBearer(String authorizationHeader) {
        return authorizationHeader != null
                && authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7);
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
