package com.lifebalance.task.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RestTaskIntegrationClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void retriesTransientCrossServiceFailuresUntilTheCallSucceeds() throws IOException {
        AtomicInteger attempts = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/notifications", exchange -> {
            exchange.getRequestBody().readAllBytes();
            int status = attempts.incrementAndGet() < 3 ? 503 : 204;
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();

        TaskIntegrationProperties properties = properties(3);
        new RestTaskIntegrationClient(RestClient.builder(), properties)
                .createNotification(notification(), "Bearer test-token");

        assertThat(attempts).hasValue(3);
    }

    @Test
    void stopsAfterTheConfiguredMaximumAttempts() throws IOException {
        AtomicInteger attempts = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/notifications", exchange -> {
            exchange.getRequestBody().readAllBytes();
            attempts.incrementAndGet();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();

        TaskIntegrationProperties properties = properties(2);
        new RestTaskIntegrationClient(RestClient.builder(), properties)
                .createNotification(notification(), "Bearer test-token");

        assertThat(attempts).hasValue(2);
    }

    @Test
    void doesNotRetryPermanentClientFailures() throws IOException {
        AtomicInteger attempts = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/notifications", exchange -> {
            exchange.getRequestBody().readAllBytes();
            attempts.incrementAndGet();
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
        });
        server.start();

        TaskIntegrationProperties properties = properties(3);
        new RestTaskIntegrationClient(RestClient.builder(), properties)
                .createNotification(notification(), "Bearer test-token");

        assertThat(attempts).hasValue(1);
    }

    private TaskIntegrationProperties properties(int maxAttempts) {
        TaskIntegrationProperties properties = new TaskIntegrationProperties();
        properties.getNotificationService().setBaseUrl(
                "http://127.0.0.1:" + server.getAddress().getPort()
        );
        properties.setMaxAttempts(maxAttempts);
        properties.setRetryBackoffMillis(0);
        return properties;
    }

    private static TaskNotificationRequest notification() {
        return new TaskNotificationRequest(
                "TASK_TEST",
                Set.of("IN_APP"),
                "NORMAL",
                "Test notification",
                "Retry integration call",
                "TASK",
                UUID.randomUUID(),
                "Regression test",
                true,
                null,
                null
        );
    }
}
