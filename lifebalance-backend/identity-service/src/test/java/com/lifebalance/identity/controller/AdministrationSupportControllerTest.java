package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.identity.dto.CreateSupportTicketRequest;
import com.lifebalance.identity.dto.SupportTicketResponse;
import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AdministrationSupportService;
import com.lifebalance.identity.service.KeycloakUserMappingService;

@ExtendWith(MockitoExtension.class)
class AdministrationSupportControllerTest {

    @Mock
    private AdministrationSupportService administrationSupportService;

    @Mock
    private KeycloakUserMappingService keycloakUserMappingService;

    private MockMvc mockMvc;
    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        AdministrationSupportController controller = new AdministrationSupportController(
                administrationSupportService,
                keycloakUserMappingService
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new JwtArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        currentUser = new CurrentUser("kc-user-1", "alice", "alice@example.com", List.of("user"));
    }

    @Test
    void createTicketMapsAuthenticatedUserAndRequestBody() throws Exception {
        UUID ticketId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        SupportTicketResponse response = SupportTicketResponse.builder()
                .id(ticketId)
                .ticketNumber("SUP-20260821-ABCDEF12")
                .requesterId(requesterId)
                .requesterEmail("alice@example.com")
                .title("Cannot access account")
                .description("I cannot sign in.")
                .status(SupportTicketStatus.NEW)
                .priority(SupportTicketPriority.HIGH)
                .category(SupportTicketCategory.ACCOUNT_ACCESS)
                .createdAt(OffsetDateTime.parse("2026-08-21T07:00:00Z"))
                .build();

        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(administrationSupportService.createTicket(eq(currentUser), any(CreateSupportTicketRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/administration-support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cannot access account",
                                  "description": "I cannot sign in.",
                                  "priority": "HIGH",
                                  "category": "ACCOUNT_ACCESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.ticketNumber").value("SUP-20260821-ABCDEF12"))
                .andExpect(jsonPath("$.requesterId").value(requesterId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.category").value("ACCOUNT_ACCESS"));

        ArgumentCaptor<CreateSupportTicketRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateSupportTicketRequest.class);
        verify(administrationSupportService).createTicket(eq(currentUser), requestCaptor.capture());
        CreateSupportTicketRequest request = requestCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(request.getTitle()).isEqualTo("Cannot access account");
        org.assertj.core.api.Assertions.assertThat(request.getPriority()).isEqualTo(SupportTicketPriority.HIGH);
    }

    @Test
    void createTicketRejectsMissingTitleBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/administration-support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "I cannot sign in.",
                                  "priority": "HIGH",
                                  "category": "ACCOUNT_ACCESS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.title").isNotEmpty());

        verify(administrationSupportService, never()).createTicket(any(), any());
    }

    private static final class JwtArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && Jwt.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", "kc-user-1")
                    .build();
        }
    }
}
