package com.lifebalance.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
class GatewayProductionTest {

    private static final String ROUTES_PROPERTY =
            "spring.cloud.gateway.server.webmvc.routes";

    private static final String IDENTITY_ROUTE_ID = "identity-service";
    private static final String IDENTITY_ROUTE_URI = "lb://IDENTITY-SERVICE";
    private static final String IDENTITY_ENDPOINT = "/api/users/me";

    private static final String CAPITAL_ROUTE_ID = "resource-capital-service";
    private static final String CAPITAL_ROUTE_URI = "lb://RESOURCE-CAPITAL-SERVICE";
    private static final String CAPITAL_SUMMARY_ENDPOINT = "/api/v1/capital/summary";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("TC_PROD_01 - Prometheus endpoint is available for monitoring")
    void tcProd01_prometheusMetrics_shouldReturn200() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/prometheus", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotBlank()
                .contains("# HELP", "# TYPE");
    }

    @Test
    @DisplayName("TC_PROD_02 - /api/users/me is mapped to Identity Service")
    void tcProd02_identityRoute_shouldMapUsersMeToIdentityService() {
        assertRoute(
                IDENTITY_ROUTE_ID,
                IDENTITY_ROUTE_URI,
                IDENTITY_ENDPOINT
        );
    }

    @Test
    @DisplayName("TC_PROD_03 - /api/v1/capital/summary is mapped to Resource Capital Service")
    void tcProd03_capitalRoute_shouldMapSummaryToResourceCapitalService() {
        assertRoute(
                CAPITAL_ROUTE_ID,
                CAPITAL_ROUTE_URI,
                CAPITAL_SUMMARY_ENDPOINT
        );
    }

    private void assertRoute(
            String routeId,
            String expectedUri,
            String requestPath
    ) {
        GatewayRoute route = findRoute(routeId);

        assertThat(route.uri())
                .as("Route %s must use the expected load-balanced service URI", routeId)
                .isEqualTo(expectedUri);

        List<String> patterns = extractPathPatterns(route.pathPredicate());

        boolean pathMatched = patterns.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, requestPath));

        assertThat(pathMatched)
                .as(
                        "Route %s must contain a Path predicate matching %s. Current patterns: %s",
                        routeId,
                        requestPath,
                        patterns
                )
                .isTrue();
    }

    private GatewayRoute findRoute(String routeId) {
        for (int index = 0; index < 50; index++) {
            String prefix = ROUTES_PROPERTY + "[" + index + "]";
            String id = environment.getProperty(prefix + ".id");

            if (id == null) {
                break;
            }

            if (routeId.equals(id)) {
                String uri = environment.getProperty(prefix + ".uri");
                String predicate =
                        environment.getProperty(prefix + ".predicates[0]", "");

                return new GatewayRoute(id, uri, predicate);
            }
        }

        fail("Gateway route was not found: " + routeId);
        throw new IllegalStateException("Unreachable");
    }

    private List<String> extractPathPatterns(String predicate) {
        assertThat(predicate)
                .as("Gateway route must contain a Path predicate")
                .startsWith("Path=");

        return Arrays.stream(predicate.substring("Path=".length()).split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isBlank())
                .toList();
    }

    private record GatewayRoute(
            String id,
            String uri,
            String pathPredicate
    ) {
    }
}