package com.lifebalance.identity.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.dto.ActivityLogResponse;
import com.lifebalance.identity.dto.AdministrationAuditLogResponse;
import com.lifebalance.identity.dto.AdministrationDashboardResponse;
import com.lifebalance.identity.dto.AdministrationReportResponse;
import com.lifebalance.identity.dto.AssignSupportTicketRequest;
import com.lifebalance.identity.dto.CreateAnnouncementRequest;
import com.lifebalance.identity.dto.CreateSupportTicketRequest;
import com.lifebalance.identity.dto.MaintenanceStatusResponse;
import com.lifebalance.identity.dto.ResolveSupportTicketRequest;
import com.lifebalance.identity.dto.SupportTicketHistoryResponse;
import com.lifebalance.identity.dto.SupportTicketResponse;
import com.lifebalance.identity.dto.SystemAnnouncementResponse;
import com.lifebalance.identity.dto.SystemConfigurationResponse;
import com.lifebalance.identity.dto.TicketCommentRequest;
import com.lifebalance.identity.dto.TicketReasonRequest;
import com.lifebalance.identity.dto.UpdateMaintenanceStatusRequest;
import com.lifebalance.identity.dto.UpdateSupportTicketRequest;
import com.lifebalance.identity.dto.UpdateSystemConfigurationRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.model.enums.AdministrationReportType;
import com.lifebalance.identity.model.enums.ActivityCategory;
import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AdministrationSupportService;
import com.lifebalance.identity.service.KeycloakUserMappingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Administration Support", description = "Administration and support operation APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/administration-support", "/api/administration-support"})
@RequiredArgsConstructor
public class AdministrationSupportController {

    private final AdministrationSupportService administrationSupportService;
    private final KeycloakUserMappingService keycloakUserMappingService;

    @GetMapping("/users")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:read')")
    public Page<UserResponse> searchUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String roleCode,
            Pageable pageable
    ) {
        return administrationSupportService.searchUsers(
                keyword,
                status,
                roleCode,
                PageableLimits.normalize(pageable)
        );
    }

    @GetMapping("/staff")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:read')")
    public Page<UserResponse> searchStaff(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) AccountStatus status,
            Pageable pageable
    ) {
        return administrationSupportService.searchStaff(
                keyword,
                status,
                PageableLimits.normalize(pageable)
        );
    }

    @PostMapping("/tickets")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:create')")
    public SupportTicketResponse createTicket(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateSupportTicketRequest request
    ) {
        return administrationSupportService.createTicket(currentUser(jwt), request);
    }

    @GetMapping("/tickets")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:read')")
    public Page<SupportTicketResponse> searchTickets(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID requesterId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) SupportTicketStatus status,
            @RequestParam(required = false) SupportTicketPriority priority,
            @RequestParam(required = false) SupportTicketCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable
    ) {
        return administrationSupportService.searchTickets(
                currentUser(jwt),
                requesterId,
                assigneeId,
                status,
                priority,
                category,
                createdFrom,
                createdTo,
                keyword,
                PageableLimits.normalize(pageable)
        );
    }

    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:read')")
    public SupportTicketResponse getTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return administrationSupportService.getTicket(currentUser(jwt), ticketId);
    }

    @GetMapping("/tickets/{ticketId}/history")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:read')")
    public List<SupportTicketHistoryResponse> getTicketHistory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return administrationSupportService.getTicketHistory(currentUser(jwt), ticketId);
    }

    @PostMapping("/tickets/{ticketId}/receive")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:update')")
    public SupportTicketResponse receiveTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return administrationSupportService.receiveTicket(currentUser(jwt), ticketId);
    }

    @PostMapping("/tickets/{ticketId}/assign")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:assign')")
    public SupportTicketResponse assignTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody AssignSupportTicketRequest request
    ) {
        return administrationSupportService.assignTicket(currentUser(jwt), ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/unassign")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:assign')")
    public SupportTicketResponse removeAssignee(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @RequestBody(required = false) TicketReasonRequest request
    ) {
        return administrationSupportService.removeAssignee(currentUser(jwt), ticketId, request);
    }

    @PatchMapping("/tickets/{ticketId}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:update')")
    public SupportTicketResponse updateTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody UpdateSupportTicketRequest request
    ) {
        return administrationSupportService.updateTicket(currentUser(jwt), ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:update')")
    public SupportTicketResponse addComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketCommentRequest request
    ) {
        return administrationSupportService.addComment(currentUser(jwt), ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/escalate")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:update')")
    public SupportTicketResponse escalateTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketReasonRequest request
    ) {
        return administrationSupportService.escalateTicket(currentUser(jwt), ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:resolve')")
    public SupportTicketResponse resolveTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody ResolveSupportTicketRequest request
    ) {
        return administrationSupportService.resolveTicket(currentUser(jwt), ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/close")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:resolve')")
    public SupportTicketResponse closeTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @RequestBody(required = false) TicketReasonRequest request
    ) {
        return administrationSupportService.closeTicket(currentUser(jwt), ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/reopen")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'support-ticket:resolve')")
    public SupportTicketResponse reopenTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketReasonRequest request
    ) {
        return administrationSupportService.reopenTicket(currentUser(jwt), ticketId, request);
    }

    @GetMapping("/activity-logs")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'activity-log:read')")
    public Page<ActivityLogResponse> searchActivityLogs(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) ActivityCategory category,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime occurredFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime occurredTo,
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable
    ) {
        return administrationSupportService.searchActivityLogs(
                actorId,
                category,
                action,
                entityType,
                entityId,
                occurredFrom,
                occurredTo,
                keyword,
                PageableLimits.normalize(pageable)
        );
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'audit:read')")
    public Page<AdministrationAuditLogResponse> searchAuditLogs(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) AuditEntityName entityName,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable
    ) {
        return administrationSupportService.searchAuditLogs(
                actorId,
                userId,
                entityName,
                action,
                createdFrom,
                createdTo,
                keyword,
                PageableLimits.normalize(pageable)
        );
    }

    @GetMapping("/configurations")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'configuration:read')")
    public List<SystemConfigurationResponse> listConfigurations() {
        return administrationSupportService.listConfigurations();
    }

    @PutMapping("/configurations/{configKey}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'configuration:update')")
    public SystemConfigurationResponse updateConfiguration(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String configKey,
            @Valid @RequestBody UpdateSystemConfigurationRequest request
    ) {
        return administrationSupportService.updateConfiguration(currentUser(jwt), configKey, request);
    }

    @PostMapping("/announcements")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'announcement:create')")
    public SystemAnnouncementResponse broadcastAnnouncement(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAnnouncementRequest request
    ) {
        return administrationSupportService.broadcastAnnouncement(currentUser(jwt), request);
    }

    @GetMapping("/announcements")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'announcement:read')")
    public Page<SystemAnnouncementResponse> searchAnnouncements(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) AnnouncementAudience audience,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startsFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startsTo,
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable
    ) {
        return administrationSupportService.searchAnnouncements(
                currentUser(jwt),
                status,
                audience,
                startsFrom,
                startsTo,
                keyword,
                PageableLimits.normalize(pageable)
        );
    }

    @GetMapping("/announcements/{announcementId}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'announcement:read')")
    public SystemAnnouncementResponse getAnnouncement(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID announcementId
    ) {
        return administrationSupportService.getAnnouncement(currentUser(jwt), announcementId);
    }

    @GetMapping("/announcements/{announcementId}/history")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'announcement:read')")
    public Page<AdministrationAuditLogResponse> getAnnouncementHistory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID announcementId,
            Pageable pageable
    ) {
        return administrationSupportService.getAnnouncementHistory(
                currentUser(jwt),
                announcementId,
                PageableLimits.normalize(pageable)
        );
    }

    @GetMapping("/maintenance-status")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'maintenance:read')")
    public MaintenanceStatusResponse maintenanceStatus() {
        return administrationSupportService.maintenanceStatus();
    }

    @PutMapping("/maintenance-status")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'maintenance:update')")
    public MaintenanceStatusResponse updateMaintenanceStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateMaintenanceStatusRequest request
    ) {
        return administrationSupportService.updateMaintenanceStatus(currentUser(jwt), request);
    }

    @PatchMapping("/maintenance-status")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'maintenance:update')")
    public MaintenanceStatusResponse patchMaintenanceStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateMaintenanceStatusRequest request
    ) {
        return administrationSupportService.updateMaintenanceStatus(currentUser(jwt), request);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationDashboardResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return administrationSupportService.dashboard(periodStart, periodEnd);
    }

    @GetMapping("/reports/tickets")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse ticketReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.TICKETS, periodStart, periodEnd);
    }

    @GetMapping("/reports/support-performance")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse supportPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.SUPPORT_PERFORMANCE, periodStart, periodEnd);
    }

    @GetMapping("/reports/user-activity")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse userActivityReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.USER_ACTIVITY, periodStart, periodEnd);
    }

    @GetMapping("/reports/role-assignments")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse roleAssignmentReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.ROLE_ASSIGNMENTS, periodStart, periodEnd);
    }

    @GetMapping("/reports/permission-changes")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse permissionChangeReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.PERMISSION_CHANGES, periodStart, periodEnd);
    }

    @GetMapping("/reports/configuration-changes")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse configurationChangeReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.CONFIGURATION_CHANGES, periodStart, periodEnd);
    }

    @GetMapping("/reports/announcements")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse announcementReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.ANNOUNCEMENTS, periodStart, periodEnd);
    }

    @GetMapping("/reports/maintenance")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'administration-dashboard:read')")
    public AdministrationReportResponse maintenanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime periodEnd
    ) {
        return report(AdministrationReportType.MAINTENANCE, periodStart, periodEnd);
    }

    private AdministrationReportResponse report(
            AdministrationReportType reportType,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {
        return administrationSupportService.report(reportType, periodStart, periodEnd);
    }

    private CurrentUser currentUser(Jwt jwt) {
        return keycloakUserMappingService.map(jwt);
    }
}
