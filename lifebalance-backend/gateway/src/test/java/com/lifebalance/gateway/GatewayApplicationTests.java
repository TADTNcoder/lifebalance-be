package com.lifebalance.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false"
)
class GatewayApplicationTests {

    @Autowired
    private Environment environment;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void identityControllerAliasesAreRouted() {
        assertThat(routePredicate("identity-service"))
                .contains("/api/users/**", "/users/**")
                .contains("/api/roles/**", "/roles/**")
                .contains("/api/permissions/**", "/permissions/**");
    }

    @Test
    void taskRouteDoesNotAdvertiseMissingTaskGroupApi() {
        assertThat(routePredicate("task-service"))
                .doesNotContain("/api/task-groups/**");
    }

    @Test
    void prometheusEndpointIsAvailableForScraping() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/prometheus", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("# HELP");
    }

    private String routePredicate(String routeId) {
        for (int index = 0; ; index++) {
            String prefix = "spring.cloud.gateway.server.webmvc.routes[" + index + "]";
            String id = environment.getProperty(prefix + ".id");
            if (id == null) {
                fail("Gateway route was not found: " + routeId);
            }
            if (routeId.equals(id)) {
                return environment.getProperty(prefix + ".predicates[0]", "");
            }
        }
    }

}
