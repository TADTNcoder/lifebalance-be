package com.lifebalance.identity.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lifebalance.identity.dto.ActivityLogResponse;
import com.lifebalance.identity.dto.AdministrationAuditLogResponse;
import com.lifebalance.identity.dto.AdministrationDashboardResponse;
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
import com.lifebalance.identity.dto.UpdateSupportTicketRequest;
import com.lifebalance.identity.dto.UpdateSystemConfigurationRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.model.enums.ActivityCategory;
import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;
import com.lifebalance.identity.security.CurrentUser;

public interface AdministrationSupportService {

    Page<UserResponse> searchUsers(
            String keyword,
            AccountStatus status,
            String roleCode,
            Pageable pageable
    );

    Page<UserResponse> searchStaff(
            String keyword,
            AccountStatus status,
            Pageable pageable
    );

    SupportTicketResponse createTicket(CurrentUser currentUser, CreateSupportTicketRequest request);

    Page<SupportTicketResponse> searchTickets(
            CurrentUser currentUser,
            UUID requesterId,
            UUID assigneeId,
            SupportTicketStatus status,
            SupportTicketPriority priority,
            SupportTicketCategory category,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            String keyword,
            Pageable pageable
    );

    SupportTicketResponse getTicket(CurrentUser currentUser, UUID ticketId);

    List<SupportTicketHistoryResponse> getTicketHistory(CurrentUser currentUser, UUID ticketId);

    SupportTicketResponse receiveTicket(CurrentUser currentUser, UUID ticketId);

    SupportTicketResponse assignTicket(CurrentUser currentUser, UUID ticketId, AssignSupportTicketRequest request);

    SupportTicketResponse removeAssignee(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request);

    SupportTicketResponse updateTicket(CurrentUser currentUser, UUID ticketId, UpdateSupportTicketRequest request);

    SupportTicketResponse addComment(CurrentUser currentUser, UUID ticketId, TicketCommentRequest request);

    SupportTicketResponse escalateTicket(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request);

    SupportTicketResponse resolveTicket(CurrentUser currentUser, UUID ticketId, ResolveSupportTicketRequest request);

    SupportTicketResponse closeTicket(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request);

    SupportTicketResponse reopenTicket(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request);

    Page<ActivityLogResponse> searchActivityLogs(
            UUID actorId,
            ActivityCategory category,
            String action,
            String entityType,
            String entityId,
            OffsetDateTime occurredFrom,
            OffsetDateTime occurredTo,
            String keyword,
            Pageable pageable
    );

    Page<AdministrationAuditLogResponse> searchAuditLogs(
            UUID actorId,
            UUID userId,
            AuditEntityName entityName,
            AuditAction action,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            String keyword,
            Pageable pageable
    );

    List<SystemConfigurationResponse> listConfigurations();

    SystemConfigurationResponse updateConfiguration(
            CurrentUser currentUser,
            String configKey,
            UpdateSystemConfigurationRequest request
    );

    SystemAnnouncementResponse broadcastAnnouncement(
            CurrentUser currentUser,
            CreateAnnouncementRequest request
    );

    Page<SystemAnnouncementResponse> searchAnnouncements(
            AnnouncementStatus status,
            AnnouncementAudience audience,
            OffsetDateTime startsFrom,
            OffsetDateTime startsTo,
            String keyword,
            Pageable pageable
    );

    MaintenanceStatusResponse maintenanceStatus();

    AdministrationDashboardResponse dashboard(OffsetDateTime periodStart, OffsetDateTime periodEnd);
}
