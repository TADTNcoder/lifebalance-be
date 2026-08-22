package com.lifebalance.resourcecapital.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleOverlapException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCyclePeriodException;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CloseCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.ReopenCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalResponse;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;
import com.lifebalance.resourcecapital.service.CapitalCycleService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final UUID TARGET_CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SOURCE_HISTORY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID TARGET_HISTORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String ENDPOINT = "/api/v1/capital-cycles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapitalCycleService capitalCycleService;

    @Test
    void listReturnsOkWhenUserIsAuthenticated() throws Exception {
        when(capitalCycleService.listCycles(
                eq(OWNER_ID),
                eq(CapitalCycleType.MONTHLY),
                eq(CapitalCycleStatus.DRAFT),
                eq(LocalDate.of(2026, 8, 1)),
                eq(LocalDate.of(2026, 8, 31)),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 5), 1));

        mockMvc.perform(get(ENDPOINT)
                        .with(authenticatedUser())
                        .param("type", "MONTHLY")
                        .param("status", "DRAFT")
                        .param("fromDate", "2026-08-01")
                        .param("toDate", "2026-08-31")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.content[0].type").value("MONTHLY"))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(capitalCycleService).listCycles(
                eq(OWNER_ID),
                eq(CapitalCycleType.MONTHLY),
                eq(CapitalCycleStatus.DRAFT),
                eq(LocalDate.of(2026, 8, 1)),
                eq(LocalDate.of(2026, 8, 31)),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void activeReturnsOkWhenActiveCycleExists() throws Exception {
        when(capitalCycleService.getActiveCycle(OWNER_ID, CapitalCycleType.MONTHLY))
                .thenReturn(Optional.of(activatedResponse()));

        mockMvc.perform(get(ENDPOINT + "/active")
                        .with(authenticatedUser())
                        .param("type", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(capitalCycleService).getActiveCycle(OWNER_ID, CapitalCycleType.MONTHLY);
    }

    @Test
    void activeReturnsNotFoundWhenActiveCycleDoesNotExist() throws Exception {
        when(capitalCycleService.getActiveCycle(OWNER_ID, CapitalCycleType.WEEKLY))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(ENDPOINT + "/active")
                        .with(authenticatedUser())
                        .param("type", "WEEKLY"))
                .andExpect(status().isNotFound());

        verify(capitalCycleService).getActiveCycle(OWNER_ID, CapitalCycleType.WEEKLY);
    }

    @Test
    void getReturnsOkWhenUserOwnsCycle() throws Exception {
        when(capitalCycleService.getCycle(OWNER_ID, CYCLE_ID)).thenReturn(response());

        mockMvc.perform(get(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(capitalCycleService).getCycle(OWNER_ID, CYCLE_ID);
    }

    @Test
    void deleteReturnsNoContentWhenCycleCanBeDeleted() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/{id}", CYCLE_ID)
                        .with(authenticatedUser()))
                .andExpect(status().isNoContent());

        verify(capitalCycleService).deleteCycle(OWNER_ID, CYCLE_ID);
    }

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

    @Test
    void activateReturnsOkWhenCycleIsActivated() throws Exception {
        CapitalCycleResponse response = activatedResponse();

        when(capitalCycleService.activateCycle(OWNER_ID, CYCLE_ID))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT + "/{id}/activate", CYCLE_ID)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.activatedAt").value("2026-08-09T03:15:30Z"));

        verify(capitalCycleService).activateCycle(OWNER_ID, CYCLE_ID);
    }

    @Test
    void activateReturnsConflictWhenActiveCycleAlreadyExists() throws Exception {
        when(capitalCycleService.activateCycle(OWNER_ID, CYCLE_ID))
                .thenThrow(new ActiveCapitalCycleAlreadyExistsException(OWNER_ID, CapitalCycleType.MONTHLY));

        mockMvc.perform(post(ENDPOINT + "/{id}/activate", CYCLE_ID)
                        .with(authenticatedUser()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ActiveCapitalCycleAlreadyExistsException.ERROR_CODE));

        verify(capitalCycleService).activateCycle(OWNER_ID, CYCLE_ID);
    }

    @Test
    void activateReturnsBadRequestWhenCycleStatusTransitionIsInvalid() throws Exception {
        when(capitalCycleService.activateCycle(OWNER_ID, CYCLE_ID))
                .thenThrow(new InvalidCapitalCycleStateException(
                        CYCLE_ID,
                        CapitalCycleStatus.CLOSED,
                        CapitalCycleStatus.ACTIVE,
                        "activate"
                ));

        mockMvc.perform(post(ENDPOINT + "/{id}/activate", CYCLE_ID)
                        .with(authenticatedUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCycleStateException.ERROR_CODE));

        verify(capitalCycleService).activateCycle(OWNER_ID, CYCLE_ID);
    }

    @Test
    void activateReturnsNotFoundWhenCycleDoesNotBelongToUser() throws Exception {
        when(capitalCycleService.activateCycle(OWNER_ID, CYCLE_ID))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(post(ENDPOINT + "/{id}/activate", CYCLE_ID)
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital cycle " + CYCLE_ID + " was not found."));

        verify(capitalCycleService).activateCycle(OWNER_ID, CYCLE_ID);
    }

    @Test
    void activateReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/{id}/activate", CYCLE_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).activateCycle(any(), any());
    }

    @Test
    void closeReturnsOkWhenCycleIsClosed() throws Exception {
        CloseCapitalCycleRequest request = closeRequest();
        CapitalCycleResponse response = closedResponse();

        when(capitalCycleService.closeCycle(eq(OWNER_ID), eq(CYCLE_ID), any(CloseCapitalCycleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT + "/{id}/close", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedAt").value("2026-08-09T04:20:45Z"))
                .andExpect(jsonPath("$.closeReason").value("Finished cycle settlement"));

        ArgumentCaptor<CloseCapitalCycleRequest> requestCaptor =
                ArgumentCaptor.forClass(CloseCapitalCycleRequest.class);
        verify(capitalCycleService).closeCycle(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getReason()).isEqualTo("Finished cycle settlement");
    }

    @Test
    void closeReturnsBadRequestWhenReasonIsBlank() throws Exception {
        CloseCapitalCycleRequest request = closeRequest();
        request.setReason(" ");

        mockMvc.perform(post(ENDPOINT + "/{id}/close", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.reason").exists());

        verify(capitalCycleService, never()).closeCycle(any(), any(), any());
    }

    @Test
    void closeReturnsBadRequestWhenTransitionIsInvalid() throws Exception {
        CloseCapitalCycleRequest request = closeRequest();

        when(capitalCycleService.closeCycle(eq(OWNER_ID), eq(CYCLE_ID), any(CloseCapitalCycleRequest.class)))
                .thenThrow(new InvalidCapitalCycleStateException(
                        CYCLE_ID,
                        CapitalCycleStatus.DRAFT,
                        CapitalCycleStatus.CLOSED,
                        "close"
                ));

        mockMvc.perform(post(ENDPOINT + "/{id}/close", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCycleStateException.ERROR_CODE));

        verify(capitalCycleService).closeCycle(eq(OWNER_ID), eq(CYCLE_ID), any(CloseCapitalCycleRequest.class));
    }

    @Test
    void closeReturnsNotFoundWhenCycleDoesNotBelongToUser() throws Exception {
        CloseCapitalCycleRequest request = closeRequest();

        when(capitalCycleService.closeCycle(eq(OWNER_ID), eq(CYCLE_ID), any(CloseCapitalCycleRequest.class)))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(post(ENDPOINT + "/{id}/close", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital cycle " + CYCLE_ID + " was not found."));

        verify(capitalCycleService).closeCycle(eq(OWNER_ID), eq(CYCLE_ID), any(CloseCapitalCycleRequest.class));
    }

    @Test
    void closeReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/{id}/close", CYCLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).closeCycle(any(), any(), any());
    }

    @Test
    void reopenReturnsOkWhenClosedCycleIsReopened() throws Exception {
        ReopenCapitalCycleRequest request = reopenRequest();
        CapitalCycleResponse response = reopenedResponse();

        when(capitalCycleService.reopenCycle(eq(OWNER_ID), eq(CYCLE_ID), any(ReopenCapitalCycleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT + "/{id}/reopen", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.status").value("REOPENED"))
                .andExpect(jsonPath("$.closedAt").value("2026-08-09T04:20:45Z"))
                .andExpect(jsonPath("$.closeReason").value("Finished cycle settlement"))
                .andExpect(jsonPath("$.reopenedAt").value("2026-08-09T05:10:00Z"))
                .andExpect(jsonPath("$.reopenReason").value("Need correction"));

        ArgumentCaptor<ReopenCapitalCycleRequest> requestCaptor =
                ArgumentCaptor.forClass(ReopenCapitalCycleRequest.class);
        verify(capitalCycleService).reopenCycle(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getReason()).isEqualTo("Need correction");
    }

    @Test
    void reopenReturnsBadRequestWhenReasonIsBlank() throws Exception {
        ReopenCapitalCycleRequest request = reopenRequest();
        request.setReason(" ");

        mockMvc.perform(post(ENDPOINT + "/{id}/reopen", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.reason").exists());

        verify(capitalCycleService, never()).reopenCycle(any(), any(), any());
    }

    @Test
    void reopenReturnsBadRequestWhenCycleIsNotClosed() throws Exception {
        ReopenCapitalCycleRequest request = reopenRequest();

        when(capitalCycleService.reopenCycle(eq(OWNER_ID), eq(CYCLE_ID), any(ReopenCapitalCycleRequest.class)))
                .thenThrow(new InvalidCapitalCycleStateException(
                        CYCLE_ID,
                        CapitalCycleStatus.ACTIVE,
                        CapitalCycleStatus.REOPENED,
                        "reopen"
                ));

        mockMvc.perform(post(ENDPOINT + "/{id}/reopen", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCycleStateException.ERROR_CODE));

        verify(capitalCycleService).reopenCycle(eq(OWNER_ID), eq(CYCLE_ID), any(ReopenCapitalCycleRequest.class));
    }

    @Test
    void reopenReturnsNotFoundWhenCycleDoesNotBelongToUser() throws Exception {
        ReopenCapitalCycleRequest request = reopenRequest();

        when(capitalCycleService.reopenCycle(eq(OWNER_ID), eq(CYCLE_ID), any(ReopenCapitalCycleRequest.class)))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(post(ENDPOINT + "/{id}/reopen", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital cycle " + CYCLE_ID + " was not found."));

        verify(capitalCycleService).reopenCycle(eq(OWNER_ID), eq(CYCLE_ID), any(ReopenCapitalCycleRequest.class));
    }

    @Test
    void reopenReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/{id}/reopen", CYCLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopenRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).reopenCycle(any(), any(), any());
    }

    @Test
    void reopenReturnsUnauthorizedWhenInternalUserIdClaimIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/{id}/reopen", CYCLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopenRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalCycleService, never()).reopenCycle(any(), any(), any());
    }

    @Test
    void transferRemainingReturnsOkWhenRequestIsConfirmedAndUserOwnsCycles() throws Exception {
        TransferRemainingCapitalRequest request = transferRequest();
        TransferRemainingCapitalResponse response = transferResponse();

        when(capitalCycleService.transferRemainingCapital(
                eq(OWNER_ID),
                eq(CYCLE_ID),
                any(TransferRemainingCapitalRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post(ENDPOINT + "/{id}/transfer-remaining", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.targetCycleId").value(TARGET_CYCLE_ID.toString()))
                .andExpect(jsonPath("$.capitalType").value("MONEY"))
                .andExpect(jsonPath("$.amount").value(125.0000))
                .andExpect(jsonPath("$.sourceBeforeAmount").value(500.0000))
                .andExpect(jsonPath("$.sourceAfterAmount").value(375.0000))
                .andExpect(jsonPath("$.targetBeforeAmount").value(100.0000))
                .andExpect(jsonPath("$.targetAfterAmount").value(225.0000))
                .andExpect(jsonPath("$.sourceHistoryId").value(SOURCE_HISTORY_ID.toString()))
                .andExpect(jsonPath("$.targetHistoryId").value(TARGET_HISTORY_ID.toString()))
                .andExpect(jsonPath("$.transferredAt").value("2026-08-09T06:00:00Z"));

        ArgumentCaptor<TransferRemainingCapitalRequest> requestCaptor =
                ArgumentCaptor.forClass(TransferRemainingCapitalRequest.class);
        verify(capitalCycleService).transferRemainingCapital(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().targetCycleId()).isEqualTo(TARGET_CYCLE_ID);
        assertThat(requestCaptor.getValue().capitalType()).isEqualTo(CapitalKind.MONEY);
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo("125.0000");
        assertThat(requestCaptor.getValue().transferConfirmed()).isTrue();
    }

    @Test
    void transferRemainingReturnsBadRequestWhenAmountIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/{id}/transfer-remaining", CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetCycleId": "33333333-3333-3333-3333-333333333333",
                                  "capitalType": "MONEY",
                                  "reason": "Carry remaining budget",
                                  "transferConfirmed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.amount").exists());

        verify(capitalCycleService, never()).transferRemainingCapital(any(), any(), any());
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

    private static CapitalCycleResponse activatedResponse() {
        CapitalCycleResponse response = response();
        response.setStatus(CapitalCycleStatus.ACTIVE);
        response.setActivatedAt(Instant.parse("2026-08-09T03:15:30Z"));
        return response;
    }

    private static CloseCapitalCycleRequest closeRequest() {
        CloseCapitalCycleRequest request = new CloseCapitalCycleRequest();
        request.setReason("Finished cycle settlement");
        return request;
    }

    private static CapitalCycleResponse closedResponse() {
        CapitalCycleResponse response = response();
        response.setStatus(CapitalCycleStatus.CLOSED);
        response.setActivatedAt(Instant.parse("2026-08-09T03:15:30Z"));
        response.setClosedAt(Instant.parse("2026-08-09T04:20:45Z"));
        response.setCloseReason("Finished cycle settlement");
        return response;
    }

    private static ReopenCapitalCycleRequest reopenRequest() {
        ReopenCapitalCycleRequest request = new ReopenCapitalCycleRequest();
        request.setReason("Need correction");
        return request;
    }

    private static CapitalCycleResponse reopenedResponse() {
        CapitalCycleResponse response = closedResponse();
        response.setStatus(CapitalCycleStatus.REOPENED);
        response.setReopenedAt(Instant.parse("2026-08-09T05:10:00Z"));
        response.setReopenReason("Need correction");
        return response;
    }

    private static TransferRemainingCapitalRequest transferRequest() {
        return new TransferRemainingCapitalRequest(
                TARGET_CYCLE_ID,
                CapitalKind.MONEY,
                new BigDecimal("125.0000"),
                "Carry remaining budget",
                true
        );
    }

    private static TransferRemainingCapitalResponse transferResponse() {
        return new TransferRemainingCapitalResponse(
                CYCLE_ID,
                TARGET_CYCLE_ID,
                CapitalKind.MONEY,
                new BigDecimal("125.0000"),
                new BigDecimal("500.0000"),
                new BigDecimal("375.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("225.0000"),
                "Carry remaining budget",
                SOURCE_HISTORY_ID,
                TARGET_HISTORY_ID,
                Instant.parse("2026-08-09T06:00:00Z")
        );
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
