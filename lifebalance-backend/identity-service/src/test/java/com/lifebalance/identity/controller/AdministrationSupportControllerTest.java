package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import com.lifebalance.identity.dto.AdministrationAuditLogResponse;
import com.lifebalance.identity.dto.AdministrationReportResponse;
import com.lifebalance.identity.dto.CreateSupportTicketRequest;
import com.lifebalance.identity.dto.MaintenanceStatusResponse;
import com.lifebalance.identity.dto.SupportTicketResponse;
import com.lifebalance.identity.dto.SystemAnnouncementResponse;
import com.lifebalance.identity.dto.UpdateMaintenanceStatusRequest;
import com.lifebalance.identity.model.enums.AdministrationReportType;
import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
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
                .setCustomArgumentResolvers(
                        new JwtArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
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

    @Test
    void getAnnouncementMapsAuthenticatedUserAndPathVariable() throws Exception {
        UUID announcementId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        SystemAnnouncementResponse response = SystemAnnouncementResponse.builder()
                .id(announcementId)
                .title("Maintenance notice")
                .message("Maintenance starts tonight.")
                .audience(AnnouncementAudience.ALL_USERS)
                .status(AnnouncementStatus.ACTIVE)
                .startsAt(OffsetDateTime.parse("2026-08-21T07:00:00Z"))
                .build();

        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(administrationSupportService.getAnnouncement(currentUser, announcementId)).thenReturn(response);

        mockMvc.perform(get("/api/administration-support/announcements/{announcementId}", announcementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(announcementId.toString()))
                .andExpect(jsonPath("$.title").value("Maintenance notice"))
                .andExpect(jsonPath("$.audience").value("ALL_USERS"));

        verify(administrationSupportService).getAnnouncement(currentUser, announcementId);
    }

    @Test
    void getAnnouncementHistoryMapsPageable() throws Exception {
        UUID announcementId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        AdministrationAuditLogResponse history = AdministrationAuditLogResponse.builder()
                .id(UUID.fromString("55555555-5555-5555-5555-555555555555"))
                .entityName(AuditEntityName.ANNOUNCEMENT)
                .entityId(announcementId.toString())
                .action(AuditAction.BROADCAST_ANNOUNCEMENT)
                .status(AuditStatus.SUCCESS)
                .createdAt(OffsetDateTime.parse("2026-08-21T07:00:00Z"))
                .build();

        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(administrationSupportService.getAnnouncementHistory(
                eq(currentUser),
                eq(announcementId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(history), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/administration-support/announcements/{announcementId}/history", announcementId)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].entityName").value("ANNOUNCEMENT"))
                .andExpect(jsonPath("$.content[0].action").value("BROADCAST_ANNOUNCEMENT"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(administrationSupportService).getAnnouncementHistory(
                eq(currentUser),
                eq(announcementId),
                pageableCaptor.capture()
        );
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void updateMaintenanceStatusMapsRequestBody() throws Exception {
        OffsetDateTime startsAt = OffsetDateTime.parse("2026-08-22T01:00:00Z");
        OffsetDateTime endsAt = OffsetDateTime.parse("2026-08-22T02:00:00Z");
        MaintenanceStatusResponse response = MaintenanceStatusResponse.builder()
                .policyEnabled(true)
                .maintenanceMode(true)
                .message("Scheduled maintenance")
                .startsAt(startsAt)
                .endsAt(endsAt)
                .build();

        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(administrationSupportService.updateMaintenanceStatus(
                eq(currentUser),
                any(UpdateMaintenanceStatusRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/administration-support/maintenance-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true,
                                  "message": "Scheduled maintenance",
                                  "startsAt": "2026-08-22T01:00:00Z",
                                  "endsAt": "2026-08-22T02:00:00Z",
                                  "reason": "Database upgrade",
                                  "confirmed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyEnabled").value(true))
                .andExpect(jsonPath("$.maintenanceMode").value(true))
                .andExpect(jsonPath("$.message").value("Scheduled maintenance"))
                .andExpect(jsonPath("$.startsAt").exists());

        ArgumentCaptor<UpdateMaintenanceStatusRequest> requestCaptor =
                ArgumentCaptor.forClass(UpdateMaintenanceStatusRequest.class);
        verify(administrationSupportService).updateMaintenanceStatus(eq(currentUser), requestCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().getEnabled()).isTrue();
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().getReason()).isEqualTo("Database upgrade");
    }

    @Test
    void ticketReportMapsPeriodToReportService() throws Exception {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");
        AdministrationReportResponse response = AdministrationReportResponse.builder()
                .reportType(AdministrationReportType.TICKETS.name())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedAt(OffsetDateTime.parse("2026-08-22T01:00:00Z"))
                .metrics(Map.of("total", 2L))
                .build();

        when(administrationSupportService.report(AdministrationReportType.TICKETS, periodStart, periodEnd))
                .thenReturn(response);

        mockMvc.perform(get("/api/administration-support/reports/tickets")
                        .param("periodStart", "2026-08-01T00:00:00Z")
                        .param("periodEnd", "2026-08-22T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("TICKETS"))
                .andExpect(jsonPath("$.metrics.total").value(2));

        verify(administrationSupportService).report(AdministrationReportType.TICKETS, periodStart, periodEnd);
    }

    @Test
    void auditReportMapsPeriodToReportService() throws Exception {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");
        AdministrationReportResponse response = AdministrationReportResponse.builder()
                .reportType(AdministrationReportType.AUDIT.name())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedAt(OffsetDateTime.parse("2026-08-22T01:00:00Z"))
                .metrics(Map.of("total", 3L))
                .build();

        when(administrationSupportService.report(AdministrationReportType.AUDIT, periodStart, periodEnd))
                .thenReturn(response);

        mockMvc.perform(get("/api/administration-support/reports/audit")
                        .param("periodStart", "2026-08-01T00:00:00Z")
                        .param("periodEnd", "2026-08-22T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("AUDIT"))
                .andExpect(jsonPath("$.metrics.total").value(3));

        verify(administrationSupportService).report(AdministrationReportType.AUDIT, periodStart, periodEnd);
    }

    @Test
    void systemOperationReportMapsPeriodToReportService() throws Exception {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");
        AdministrationReportResponse response = AdministrationReportResponse.builder()
                .reportType(AdministrationReportType.SYSTEM_OPERATION.name())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedAt(OffsetDateTime.parse("2026-08-22T01:00:00Z"))
                .metrics(Map.of("total", 4L))
                .build();

        when(administrationSupportService.report(
                AdministrationReportType.SYSTEM_OPERATION,
                periodStart,
                periodEnd
        )).thenReturn(response);

        mockMvc.perform(get("/api/administration-support/reports/system-operation")
                        .param("periodStart", "2026-08-01T00:00:00Z")
                        .param("periodEnd", "2026-08-22T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("SYSTEM_OPERATION"))
                .andExpect(jsonPath("$.metrics.total").value(4));

        verify(administrationSupportService).report(
                AdministrationReportType.SYSTEM_OPERATION,
                periodStart,
                periodEnd
        );
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
