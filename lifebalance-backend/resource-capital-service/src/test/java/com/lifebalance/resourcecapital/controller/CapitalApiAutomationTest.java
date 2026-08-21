package com.lifebalance.resourcecapital.controller;

import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import com.lifebalance.resourcecapital.service.CapitalAllocationService;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CapitalApiAutomationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapitalAllocationService capitalAllocationService;

    @MockitoBean
    private CapitalAdjustmentService capitalAdjustmentService;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private KeycloakUserPrincipal getMockUser() {
        KeycloakUserPrincipal mockUser = Mockito.mock(KeycloakUserPrincipal.class);
        Mockito.when(mockUser.userId()).thenReturn(UUID.randomUUID());
        return mockUser;
    }

    @Test
    @WithMockUser(username = "qa_automation", authorities = {"CAPITAL_WRITE"})
    @DisplayName("LB-1863: Phân bổ vốn thành công (Happy Path)")
    void testAllocateCapitalSuccess() throws Exception {
        AllocationResponse mockAllocation = Mockito.mock(AllocationResponse.class);

        Mockito.lenient().when(capitalAllocationService.allocateCapital(
                ArgumentMatchers.any(UUID.class),
                ArgumentMatchers.any(CreateCapitalAllocationRequest.class)
        )).thenReturn(mockAllocation);

        String validPayload = """
                {
                  "capitalCycleId": "550e8400-e29b-41d4-a716-446655440000",
                  "resourceType": "MONEY",
                  "targetType": "TASK",
                  "targetId": "123e4567-e89b-12d3-a456-426614174000",
                  "amount": 500000,
                  "reason": "Automation Test Allocation Success"
                }
                """;

        mockMvc.perform(post("/api/v1/capital-allocations")
                        .requestAttr(CURRENT_USER_ATTRIBUTE, getMockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "qa_automation", authorities = {"CAPITAL_WRITE"})
    @DisplayName("LB-1866: Bắt lỗi Validation khi gửi thiếu trường dữ liệu bắt buộc")
    void testAllocateCapitalValidationFail() throws Exception {
        String invalidPayload = """
                {
                  "resourceType": "MONEY",
                  "targetType": "TASK",
                  "targetId": "123e4567-e89b-12d3-a456-426614174000",
                  "amount": 500000,
                  "reason": "Testing Validation Limit"
                }
                """;

        mockMvc.perform(post("/api/v1/capital-allocations")
                        .requestAttr(CURRENT_USER_ATTRIBUTE, getMockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(username = "qa_automation", authorities = {"CAPITAL_WRITE"})
    @DisplayName("LB-1861: Điều chỉnh tăng/giảm vốn thành công")
    void testAdjustCapitalSuccess() throws Exception {
        CapitalAdjustmentResponse mockResponse = Mockito.mock(CapitalAdjustmentResponse.class);
        Mockito.when(mockResponse.id()).thenReturn(1L);

        Mockito.lenient().when(capitalAdjustmentService.adjustCapital(
                ArgumentMatchers.any(UUID.class),
                ArgumentMatchers.any(CapitalAdjustmentRequest.class)
        )).thenReturn(mockResponse);

        String adjustPayload = """
                {
                  "capitalCycleId": "550e8400-e29b-41d4-a716-446655440000",
                  "capitalType": "MONEY",
                  "adjustmentType": "INCREASE",
                  "amount": 2000000,
                  "reason": "Automation Test Increase Capital"
                }
                """;

        mockMvc.perform(post("/api/v1/capital-adjustments")
                        .requestAttr(CURRENT_USER_ATTRIBUTE, getMockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adjustPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}