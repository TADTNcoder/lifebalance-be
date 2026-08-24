package com.lifebalance.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewayProductionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // 1. Test Actuator Prometheus (Giám sát hệ thống)
    @Test
    public void testPrometheusMetrics_ShouldReturn200() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("# HELP");
    }

    // 2. Test Gateway Routing trỏ xuống Identity Service
    @Test
    public void testGatewayRouting_ToIdentity_ShouldNotReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/me", String.class);

        // Nếu route đúng nhưng chưa login, Gateway sẽ nhả 401 Unauthorized
        // Tuyệt đối không được ra 404 (Lạc đường)
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.NOT_FOUND);
    }

    // 3. Test Gateway Routing trỏ xuống Resource Capital Service
    @Test
    public void testGatewayRouting_ToCapital_ShouldNotReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/capitals/summary", String.class);

        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.NOT_FOUND);
    }
}