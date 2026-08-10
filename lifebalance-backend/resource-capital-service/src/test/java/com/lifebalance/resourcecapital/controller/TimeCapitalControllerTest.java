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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.security.keycloak.LifebalanceSecurityAutoConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(TimeCapitalController.class)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        LifebalanceSecurityAutoConfiguration.class,
        TimeCapitalControllerTest.TestSecuritySupport.class
})
class TimeCapitalControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TIME_CAPITAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String ENDPOINT = "/api/v1/capital-cycles/{cycleId}/time-capital";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapitalService capitalService;

    @Test
    void setupReturnsCreatedWhenRequestIsValidAndUserIsAuthenticated() throws Exception {
        SetupTimeCapitalRequest request = setupRequest();
        TimeCapitalResponse response = response();

        when(capitalService.setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/capital-cycles/" + CYCLE_ID + "/time-capital"))
                .andExpect(jsonPath("$.id").value(TIME_CAPITAL_ID.toString()))
                .andExpect(jsonPath("$.cycleId").value(CYCLE_ID.toString()))
                .andExpect(jsonPath("$.plannedMinutes").value(9600))
                .andExpect(jsonPath("$.allocatedMinutes").value(0))
                .andExpect(jsonPath("$.availableMinutes").value(9600))
                .andExpect(jsonPath("$.remainingMinutes").value(9600))
                .andExpect(jsonPath("$.initialized").value(true));

        ArgumentCaptor<SetupTimeCapitalRequest> requestCaptor =
                ArgumentCaptor.forClass(SetupTimeCapitalRequest.class);
        verify(capitalService).setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().plannedMinutes()).isEqualTo(9600L);
    }

    @Test
    void setupReturnsCreatedWhenPlannedMinutesIsZero() throws Exception {
        SetupTimeCapitalRequest request = new SetupTimeCapitalRequest(0L);
        TimeCapitalResponse response = new TimeCapitalResponse(
                TIME_CAPITAL_ID,
                CYCLE_ID,
                0L,
                0L,
                0L,
                0L,
                true
        );

        when(capitalService.setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plannedMinutes").value(0))
                .andExpect(jsonPath("$.availableMinutes").value(0))
                .andExpect(jsonPath("$.remainingMinutes").value(0))
                .andExpect(jsonPath("$.initialized").value(true));

        verify(capitalService).setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class));
    }

    @Test
    void setupReturnsBadRequestWhenPlannedMinutesIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedMinutes").exists());

        verifyNoInteractions(capitalService);
    }

    @Test
    void setupReturnsBadRequestWhenPlannedMinutesIsNegative() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plannedMinutes": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedMinutes").exists());

        verifyNoInteractions(capitalService);
    }

    @Test
    void setupReturnsBadRequestWhenCycleStatusDoesNotAllowSetup() throws Exception {
        SetupTimeCapitalRequest request = setupRequest();

        when(capitalService.setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class)))
                .thenThrow(new InvalidCapitalCycleStateException(
                        CYCLE_ID,
                        CapitalCycleStatus.ACTIVE,
                        "initialize time capital",
                        "time capital initialization is allowed only while the cycle accepts initial capital setup"
                ));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(InvalidCapitalCycleStateException.ERROR_CODE));

        verify(capitalService).setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class));
    }

    @Test
    void setupReturnsConflictWhenTimeCapitalAlreadyExists() throws Exception {
        SetupTimeCapitalRequest request = setupRequest();

        when(capitalService.setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class)))
                .thenThrow(new CapitalAlreadyInitializedException(CYCLE_ID, CapitalKind.TIME));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TIME_CAPITAL_ALREADY_EXISTS"));

        verify(capitalService).setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class));
    }

    @Test
    void setupReturnsNotFoundWhenCycleDoesNotExistOrBelongToUser() throws Exception {
        SetupTimeCapitalRequest request = setupRequest();

        when(capitalService.setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class)))
                .thenThrow(new CapitalCycleNotFoundException(CYCLE_ID));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE));

        verify(capitalService).setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class));
    }

    @Test
    void setupReturnsForbiddenWhenAccessIsDenied() throws Exception {
        SetupTimeCapitalRequest request = setupRequest();

        when(capitalService.setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class)))
                .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(capitalService).setupTimeCapital(eq(OWNER_ID), eq(CYCLE_ID), any(SetupTimeCapitalRequest.class));
    }

    @Test
    void setupReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalService, never()).setupTimeCapital(any(), any(), any());
    }

    @Test
    void setupReturnsUnauthorizedWhenInternalUserIdClaimIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT, CYCLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(capitalService, never()).setupTimeCapital(any(), any(), any());
    }

    private static SetupTimeCapitalRequest setupRequest() {
        return new SetupTimeCapitalRequest(9600L);
    }

    private static TimeCapitalResponse response() {
        return new TimeCapitalResponse(
                TIME_CAPITAL_ID,
                CYCLE_ID,
                9600L,
                0L,
                9600L,
                9600L,
                true
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
