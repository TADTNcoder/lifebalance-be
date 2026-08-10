package com.lifebalance.resourcecapital.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleOverlapException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCyclePeriodException;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;
import com.lifebalance.resourcecapital.service.CapitalCycleService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(CapitalCycleController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        CapitalCycleControllerTest.TestSecuritySupport.class
})
class CapitalCycleControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String ENDPOINT = "/api/v1/capital-cycles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapitalCycleService capitalCycleService;

    @Test
    void createReturnsCreatedWhenRequestIsValidAndUserIsAuthenticated() throws Exception {
        CreateCapitalCycleRequest request = monthlyRequest();
        CapitalCycleResponse response = response();

        when(capitalCycleService.createCycle(eq(OWNER_ID), any(CreateCapitalCycleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/capital-cycles/" + CYCLE_ID))
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.name").value("Chu ky Thang 8/2026"))
                .andExpect(jsonPath("$.type").value("MONTHLY"))
                .andExpect(jsonPath("$.startDate").value("2026-08-01"))
                .andExpect(jsonPath("$.endDate").value("2026-08-31"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        ArgumentCaptor<CreateCapitalCycleRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateCapitalCycleRequest.class);
        verify(capitalCycleService).createCycle(eq(OWNER_ID), requestCaptor.capture());
        CreateCapitalCycleRequest captured = requestCaptor.getValue();
        assertThat(captured.getName()).isEqualTo("Chu ky Thang 8/2026");
        assertThat(captured.getType()).isEqualTo(CapitalCycleType.MONTHLY);
        assertThat(captured.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(captured.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 31));
    }

    @Test
    void createReturnsBadRequestWhenNameIsBlank() throws Exception {
        CreateCapitalCycleRequest request = monthlyRequest();
        request.setName("");

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.name").exists());

        verifyNoInteractions(capitalCycleService);
    }

    @Test
    void createReturnsBadRequestWhenTypeIsMissing() throws Exception {
        CreateCapitalCycleRequest request = monthlyRequest();
        request.setType(null);

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.type").exists());

        verifyNoInteractions(capitalCycleService);
    }

    @Test
    void createReturnsBadRequestWhenDatesAreMissing() throws Exception {
        CreateCapitalCycleRequest request = monthlyRequest();
        request.setStartDate(null);
        request.setEndDate(null);

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.startDate").exists())
                .andExpect(jsonPath("$.error.details.endDate").exists());

        verifyNoInteractions(capitalCycleService);
    }

    @Test
    void createReturnsBadRequestWhenServiceRejectsInvalidPeriod() throws Exception {
        CreateCapitalCycleRequest request = monthlyRequest();
        request.setStartDate(LocalDate.of(2026, 8, 31));
        request.setEndDate(LocalDate.of(2026, 8, 1));

        when(capitalCycleService.createCycle(eq(OWNER_ID), any(CreateCapitalCycleRequest.class)))
                .thenThrow(new InvalidCapitalCyclePeriodException("Capital cycle startDate must not be after endDate."));

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCyclePeriodException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital cycle startDate must not be after endDate."));

        verify(capitalCycleService).createCycle(eq(OWNER_ID), any(CreateCapitalCycleRequest.class));
    }

    @Test
    void createReturnsBadRequestWhenServiceRejectsOverlappingPeriod() throws Exception {
        CreateCapitalCycleRequest request = monthlyRequest();

        when(capitalCycleService.createCycle(eq(OWNER_ID), any(CreateCapitalCycleRequest.class)))
                .thenThrow(new CapitalCycleOverlapException(
                        OWNER_ID,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                ));

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleOverlapException.ERROR_CODE));

        verify(capitalCycleService).createCycle(eq(OWNER_ID), any(CreateCapitalCycleRequest.class));
    }

    @Test
    void createReturnsBadRequestWhenTypeValueIsUnsupported() throws Exception {
        String request = """
                {
                  "name": "Chu ky Thang 8/2026",
                  "type": "YEARLY",
                  "startDate": "2026-08-01",
                  "endDate": "2026-08-31",
                  "description": "Resource cycle"
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED));

        verifyNoInteractions(capitalCycleService);
    }

    @Test
    void createReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(monthlyRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).createCycle(any(), any());
    }

    @Test
    void createReturnsUnauthorizedWhenInternalUserIdClaimIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(monthlyRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).createCycle(any(), any());
    }

    @Test
    void updateReturnsOkWhenRequestIsValidAndUserOwnsCycle() throws Exception {
        UpdateCapitalCycleRequest request = updateRequest();
        CapitalCycleResponse response = updatedResponse();

        when(capitalCycleService.updateCycle(eq(OWNER_ID), eq(CYCLE_ID), any(UpdateCapitalCycleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.name").value("Chu ky Thang 8/2026 dieu chinh"))
                .andExpect(jsonPath("$.type").value("MONTHLY"))
                .andExpect(jsonPath("$.startDate").value("2026-08-01"))
                .andExpect(jsonPath("$.endDate").value("2026-08-31"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        ArgumentCaptor<UpdateCapitalCycleRequest> requestCaptor =
                ArgumentCaptor.forClass(UpdateCapitalCycleRequest.class);
        verify(capitalCycleService).updateCycle(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        UpdateCapitalCycleRequest captured = requestCaptor.getValue();
        assertThat(captured.getName()).isEqualTo("Chu ky Thang 8/2026 dieu chinh");
        assertThat(captured.getType()).isEqualTo(CapitalCycleType.MONTHLY);
        assertThat(captured.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(captured.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 31));
    }

    @Test
    void updateReturnsBadRequestWhenNameIsBlank() throws Exception {
        UpdateCapitalCycleRequest request = updateRequest();
        request.setName(" ");

        mockMvc.perform(put(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.name").exists());

        verify(capitalCycleService, never()).updateCycle(any(), any(), any());
    }

    @Test
    void updateReturnsBadRequestWhenStartDateIsAfterEndDate() throws Exception {
        UpdateCapitalCycleRequest request = updateRequest();
        request.setStartDate(LocalDate.of(2026, 8, 31));
        request.setEndDate(LocalDate.of(2026, 8, 1));

        mockMvc.perform(put(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.dateRangeValid").exists());

        verify(capitalCycleService, never()).updateCycle(any(), any(), any());
    }

    @Test
    void updateReturnsBadRequestWhenCycleIsNotDraft() throws Exception {
        UpdateCapitalCycleRequest request = updateRequest();

        when(capitalCycleService.updateCycle(eq(OWNER_ID), eq(CYCLE_ID), any(UpdateCapitalCycleRequest.class)))
                .thenThrow(new InvalidCapitalCycleStateException(
                        CYCLE_ID,
                        CapitalCycleStatus.ACTIVE,
                        "update information",
                        "capital cycle information can be updated only while the cycle is in DRAFT status"
                ));

        mockMvc.perform(put(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCycleStateException.ERROR_CODE));

        verify(capitalCycleService).updateCycle(eq(OWNER_ID), eq(CYCLE_ID), any(UpdateCapitalCycleRequest.class));
    }

    @Test
    void updateReturnsNotFoundWhenCycleDoesNotBelongToUser() throws Exception {
        UpdateCapitalCycleRequest request = updateRequest();

        when(capitalCycleService.updateCycle(eq(OWNER_ID), eq(CYCLE_ID), any(UpdateCapitalCycleRequest.class)))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(put(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital cycle " + CYCLE_ID + " was not found."));

        verify(capitalCycleService).updateCycle(eq(OWNER_ID), eq(CYCLE_ID), any(UpdateCapitalCycleRequest.class));
    }

    @Test
    void updateReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(put(ENDPOINT + "/{id}", CYCLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).updateCycle(any(), any(), any());
    }

    private static CreateCapitalCycleRequest monthlyRequest() {
        CreateCapitalCycleRequest request = new CreateCapitalCycleRequest();
        request.setName("Chu ky Thang 8/2026");
        request.setType(CapitalCycleType.MONTHLY);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));
        request.setDescription("Resource cycle");
        return request;
    }

    private static CapitalCycleResponse response() {
        CapitalCycleResponse response = new CapitalCycleResponse();
        response.setId(CYCLE_ID);
        response.setName("Chu ky Thang 8/2026");
        response.setType(CapitalCycleType.MONTHLY);
        response.setStartDate(LocalDate.of(2026, 8, 1));
        response.setEndDate(LocalDate.of(2026, 8, 31));
        response.setDescription("Resource cycle");
        response.setStatus(CapitalCycleStatus.DRAFT);
        return response;
    }

    private static UpdateCapitalCycleRequest updateRequest() {
        UpdateCapitalCycleRequest request = new UpdateCapitalCycleRequest();
        request.setName("Chu ky Thang 8/2026 dieu chinh");
        request.setType(CapitalCycleType.MONTHLY);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));
        request.setDescription("Cap nhat muc tieu va ke hoach chu ky");
        return request;
    }

    private static CapitalCycleResponse updatedResponse() {
        CapitalCycleResponse response = response();
        response.setName("Chu ky Thang 8/2026 dieu chinh");
        response.setDescription("Cap nhat muc tieu va ke hoach chu ky");
        return response;
    }

    private static RequestPostProcessor authenticatedUser() {
        return jwt().jwt(jwt -> jwt
                .subject("kc-user-123")
                .claim("lifebalance_user_id", OWNER_ID.toString())
        );
    }

    @TestConfiguration
    static class TestSecuritySupport {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("JWT decoding is not used by this test");
            };
        }
    }
}
