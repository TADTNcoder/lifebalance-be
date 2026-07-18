package com.lifebalance.identity.controller;

import com.lifebalance.identity.service.DatabaseHealthChecker;
import com.lifebalance.identity.service.HealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private DatabaseHealthChecker databaseHealthChecker;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "spring.application.name",
                        "identity-service"
                )
                .withProperty(
                        "info.app.version",
                        "1.0.0"
                );

        environment.setActiveProfiles("test");

        HealthService healthService = new HealthService(
                databaseHealthChecker,
                environment
        );

        HealthController healthController =
                new HealthController(healthService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(healthController)
                .build();
    }

    @Test
    void shouldReturn200WhenDatabaseIsHealthy() throws Exception {
        when(databaseHealthChecker.isHealthy()).thenReturn(true);

        mockMvc.perform(get("/api/identity/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.uptime")
                        .value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.serviceName")
                        .value("identity-service"))
                .andExpect(jsonPath("$.environment").value("test"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.checks.database").value("ok"));
    }

    @Test
    void shouldReturn503WhenDatabaseIsUnhealthy() throws Exception {
        when(databaseHealthChecker.isHealthy()).thenReturn(false);

        mockMvc.perform(get("/api/identity/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.serviceName")
                        .value("identity-service"))
                .andExpect(jsonPath("$.checks.database")
                        .value("error"));
    }
}