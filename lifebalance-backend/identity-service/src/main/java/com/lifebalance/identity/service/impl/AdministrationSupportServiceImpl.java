package com.lifebalance.identity.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.web.PageableLimits;
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
import com.lifebalance.identity.exception.AdministrationSupportException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.model.ActivityLog;
import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.SupportTicket;
import com.lifebalance.identity.model.SupportTicketHistory;
import com.lifebalance.identity.model.SystemAnnouncement;
import com.lifebalance.identity.model.SystemConfiguration;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.model.enums.AdministrationReportType;
import com.lifebalance.identity.model.enums.ActivityCategory;
import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;
import com.lifebalance.identity.model.enums.SystemConfigurationValueType;
import com.lifebalance.identity.model.enums.TicketHistoryAction;
import com.lifebalance.identity.repository.ActivityLogRepository;
import com.lifebalance.identity.repository.AuditLogRepository;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.repository.SupportTicketHistoryRepository;
import com.lifebalance.identity.repository.SupportTicketRepository;
import com.lifebalance.identity.repository.SystemAnnouncementRepository;
import com.lifebalance.identity.repository.SystemConfigurationRepository;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AdministrationSupportService;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdministrationSupportServiceImpl implements AdministrationSupportService {

    private static final Set<String> STAFF_ROLE_CODES = Set.of("admin", "manager", "staff", "support");
    private static final Set<SupportTicketStatus> OPEN_STATUSES = Set.of(
            SupportTicketStatus.NEW,
            SupportTicketStatus.RECEIVED,
            SupportTicketStatus.ASSIGNED,
            SupportTicketStatus.IN_PROGRESS,
            SupportTicketStatus.ESCALATED,
            SupportTicketStatus.REOPENED
    );
    private static final DateTimeFormatter TICKET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ANNOUNCEMENT_POLICY_KEY = "announcement.policy.enabled";
    private static final String MAINTENANCE_POLICY_KEY = "maintenance.policy.enabled";
    private static final String MAINTENANCE_MODE_KEY = "maintenance.mode.enabled";
    private static final String MAINTENANCE_MESSAGE_KEY = "maintenance.message";
    private static final String MAINTENANCE_WINDOW_STARTS_AT_KEY = "maintenance.window.starts_at";
    private static final String MAINTENANCE_WINDOW_ENDS_AT_KEY = "maintenance.window.ends_at";

    private final InternalUserService internalUserService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketHistoryRepository supportTicketHistoryRepository;
    private final ActivityLogRepository activityLogRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final SystemAnnouncementRepository systemAnnouncementRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    public Page<UserResponse> searchUsers(
            String keyword,
            AccountStatus status,
            String roleCode,
            Pageable pageable
    ) {
        return userRepository.searchForAdministration(
                keyword(keyword),
                status,
                normalize(roleCode),
                PageableLimits.normalize(pageable)
        ).map(this::toUserResponse);
    }

    @Override
    public Page<UserResponse> searchStaff(
            String keyword,
            AccountStatus status,
            Pageable pageable
    ) {
        return userRepository.searchForAdministration(
                keyword(keyword),
                status,
                "manager",
                PageableLimits.normalize(pageable)
        ).map(this::toUserResponse);
    }

    @Override
    @Transactional
    public SupportTicketResponse createTicket(CurrentUser currentUser, CreateSupportTicketRequest request) {
        ActorContext actor = actorContext(currentUser);
        validateCreateTicketRequest(request);

        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(nextTicketNumber())
                .requester(actor.user())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .status(SupportTicketStatus.NEW)
                .lastStatusChangedAt(OffsetDateTime.now())
                .build();

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        recordTicketHistory(
                savedTicket,
                actor.user(),
                TicketHistoryAction.CREATED,
                null,
                savedTicket.getStatus(),
                null,
                null,
                null,
                "Ticket created",
                null,
                savedTicket.getTitle()
        );
        recordActivity(
                actor.user(),
                ActivityCategory.SUPPORT_TICKET,
                "CREATE_SUPPORT_TICKET",
                "SUPPORT_TICKET",
                savedTicket.getId().toString(),
                "Support ticket created",
                savedTicket.getTicketNumber()
        );
        saveAudit(
                actor.user(),
                AuditEntityName.SUPPORT_TICKET,
                savedTicket.getId().toString(),
                AuditAction.CREATE_SUPPORT_TICKET,
                null,
                savedTicket.getTitle(),
                "Support ticket created"
        );

        return toTicketResponse(savedTicket);
    }

    @Override
    public Page<SupportTicketResponse> searchTickets(
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
    ) {
        ActorContext actor = actorContext(currentUser);
        validatePeriod(createdFrom, createdTo);

        UUID effectiveRequesterId = actor.canManageSupport()
                ? requesterId
                : actor.user().getId();
        return supportTicketRepository.search(
                effectiveRequesterId,
                actor.canManageSupport() ? assigneeId : null,
                status,
                priority,
                category,
                createdFrom,
                createdTo,
                keyword(keyword),
                PageableLimits.normalize(pageable)
        ).map(this::toTicketResponse);
    }

    @Override
    public SupportTicketResponse getTicket(CurrentUser currentUser, UUID ticketId) {
        ActorContext actor = actorContext(currentUser);
        SupportTicket ticket = findTicket(ticketId);
        assertCanViewTicket(actor, ticket);

        return toTicketResponse(ticket);
    }

    @Override
    public List<SupportTicketHistoryResponse> getTicketHistory(CurrentUser currentUser, UUID ticketId) {
        ActorContext actor = actorContext(currentUser);
        SupportTicket ticket = findTicket(ticketId);
        assertCanViewTicket(actor, ticket);

        return supportTicketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toTicketHistoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupportTicketResponse receiveTicket(CurrentUser currentUser, UUID ticketId) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "receive ticket");
        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "received");
        if (ticket.getStatus() != SupportTicketStatus.NEW && ticket.getStatus() != SupportTicketStatus.REOPENED) {
            throw AdministrationSupportException.invalidTicketState(ticket.getId(), ticket.getStatus(), "received");
        }

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(SupportTicketStatus.RECEIVED);
        ticket.setAssignee(actor.user());
        ticket.setReceivedAt(now);
        ticket.setAssignedAt(now);
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.RECEIVED,
                previousStatus,
                previousAssigneeId,
                "Ticket received",
                null,
                "RECEIVED"
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse assignTicket(
            CurrentUser currentUser,
            UUID ticketId,
            AssignSupportTicketRequest request
    ) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "assign ticket");
        if (request == null || request.getAssigneeId() == null) {
            throw AdministrationSupportException.validation("Assignee id is required");
        }

        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "assigned");
        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new UserNotFoundException(request.getAssigneeId()));
        assertStaffUser(assignee);

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setAssignee(assignee);
        ticket.setAssignedAt(now);
        ticket.setStatus(SupportTicketStatus.ASSIGNED);
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.ASSIGNED,
                previousStatus,
                previousAssigneeId,
                request.getReason(),
                value(previousAssigneeId),
                value(assignee.getId())
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse removeAssignee(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "remove ticket assignee");
        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "unassigned");
        if (ticket.getAssignee() == null) {
            throw AdministrationSupportException.invalidTicketState(ticket.getId(), ticket.getStatus(), "unassigned");
        }

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setAssignee(null);
        ticket.setAssignedAt(null);
        if (ticket.getStatus() != SupportTicketStatus.ESCALATED) {
            ticket.setStatus(SupportTicketStatus.RECEIVED);
        }
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.UNASSIGNED,
                previousStatus,
                previousAssigneeId,
                request == null ? null : request.getReason(),
                value(previousAssigneeId),
                null
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse updateTicket(
            CurrentUser currentUser,
            UUID ticketId,
            UpdateSupportTicketRequest request
    ) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "update ticket");
        validateUpdateTicketRequest(request);
        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "updated");

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        String oldValue = ticketSnapshot(ticket);

        applyTicketUpdates(ticket, request);
        promoteInProgress(ticket);
        String newValue = ticketSnapshot(ticket);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.UPDATED,
                previousStatus,
                previousAssigneeId,
                request.getReason(),
                oldValue,
                newValue
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse addComment(
            CurrentUser currentUser,
            UUID ticketId,
            TicketCommentRequest request
    ) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "comment ticket");
        if (request == null || blank(request.getComment())) {
            throw AdministrationSupportException.validation("Ticket comment is required");
        }

        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "commented");
        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        promoteInProgress(ticket);

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        recordTicketHistory(
                savedTicket,
                actor.user(),
                TicketHistoryAction.COMMENTED,
                previousStatus,
                savedTicket.getStatus(),
                previousAssigneeId,
                userId(savedTicket.getAssignee()),
                request.getComment(),
                null,
                null,
                request.getComment()
        );
        recordTicketActivity(actor.user(), savedTicket, "COMMENT_TICKET", "Ticket comment added");

        return toTicketResponse(savedTicket);
    }

    @Override
    @Transactional
    public SupportTicketResponse escalateTicket(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "escalate ticket");
        String reason = requiredReason(request, "Escalation reason is required");
        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "escalated");

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(SupportTicketStatus.ESCALATED);
        ticket.setEscalationReason(reason);
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.ESCALATED,
                previousStatus,
                previousAssigneeId,
                reason,
                value(previousStatus),
                "ESCALATED"
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse resolveTicket(
            CurrentUser currentUser,
            UUID ticketId,
            ResolveSupportTicketRequest request
    ) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "resolve ticket");
        if (request == null || blank(request.getResolution())) {
            throw AdministrationSupportException.validation("Ticket resolution is required");
        }
        SupportTicket ticket = findTicketForUpdate(ticketId);
        assertOpen(ticket, "resolved");

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(SupportTicketStatus.RESOLVED);
        ticket.setResolution(request.getResolution().trim());
        ticket.setResolvedAt(now);
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.RESOLVED,
                previousStatus,
                previousAssigneeId,
                request.getResolution(),
                value(previousStatus),
                "RESOLVED"
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse closeTicket(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "close ticket");
        SupportTicket ticket = findTicketForUpdate(ticketId);
        if (ticket.getStatus() != SupportTicketStatus.RESOLVED) {
            throw AdministrationSupportException.invalidTicketState(ticket.getId(), ticket.getStatus(), "closed");
        }

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(SupportTicketStatus.CLOSED);
        ticket.setClosedAt(now);
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.CLOSED,
                previousStatus,
                previousAssigneeId,
                request == null ? null : request.getReason(),
                value(previousStatus),
                "CLOSED"
        );
    }

    @Override
    @Transactional
    public SupportTicketResponse reopenTicket(CurrentUser currentUser, UUID ticketId, TicketReasonRequest request) {
        ActorContext actor = actorContext(currentUser);
        assertCanManageSupport(actor, "reopen ticket");
        String reason = requiredReason(request, "Reopen reason is required");
        SupportTicket ticket = findTicketForUpdate(ticketId);
        if (ticket.getStatus() != SupportTicketStatus.CLOSED) {
            throw AdministrationSupportException.invalidTicketState(ticket.getId(), ticket.getStatus(), "reopened");
        }

        SupportTicketStatus previousStatus = ticket.getStatus();
        UUID previousAssigneeId = userId(ticket.getAssignee());
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(SupportTicketStatus.REOPENED);
        ticket.setReopenedAt(now);
        ticket.setLastStatusChangedAt(now);

        return saveTicketMutation(
                ticket,
                actor.user(),
                TicketHistoryAction.REOPENED,
                previousStatus,
                previousAssigneeId,
                reason,
                value(previousStatus),
                "REOPENED"
        );
    }

    @Override
    public Page<ActivityLogResponse> searchActivityLogs(
            UUID actorId,
            ActivityCategory category,
            String action,
            String entityType,
            String entityId,
            OffsetDateTime occurredFrom,
            OffsetDateTime occurredTo,
            String keyword,
            Pageable pageable
    ) {
        validatePeriod(occurredFrom, occurredTo);
        return activityLogRepository.search(
                actorId,
                category,
                normalize(action),
                normalize(entityType),
                normalize(entityId),
                occurredFrom,
                occurredTo,
                keyword(keyword),
                PageableLimits.normalize(pageable)
        ).map(this::toActivityLogResponse);
    }

    @Override
    public Page<AdministrationAuditLogResponse> searchAuditLogs(
            UUID actorId,
            UUID userId,
            AuditEntityName entityName,
            AuditAction action,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            String keyword,
            Pageable pageable
    ) {
        validatePeriod(createdFrom, createdTo);
        return auditLogRepository.search(
                actorId,
                userId,
                entityName,
                action,
                createdFrom,
                createdTo,
                keyword(keyword),
                PageableLimits.normalize(pageable)
        ).map(this::toAuditLogResponse);
    }

    @Override
    public List<SystemConfigurationResponse> listConfigurations() {
        return systemConfigurationRepository.findAllByOrderByConfigKeyAsc()
                .stream()
                .map(this::toConfigurationResponse)
                .toList();
    }

    @Override
    @Transactional
    public SystemConfigurationResponse updateConfiguration(
            CurrentUser currentUser,
            String configKey,
            UpdateSystemConfigurationRequest request
    ) {
        ActorContext actor = actorContext(currentUser);
        validateConfigurationRequest(request);
        String normalizedKey = requireConfigKey(configKey);
        SystemConfiguration configuration = systemConfigurationRepository.findByConfigKey(normalizedKey)
                .orElseThrow(() -> AdministrationSupportException.configurationNotFound(normalizedKey));
        validateConfigurationUpdate(configuration, request);

        String oldValue = configuration.getConfigValue();
        configuration.setConfigValue(request.getValue());
        configuration.setUpdatedBy(actor.user());
        configuration.setLastChangeReason(request.getReason());
        SystemConfiguration savedConfiguration = systemConfigurationRepository.save(configuration);

        ActivityCategory category = normalizedKey.startsWith("maintenance.")
                ? ActivityCategory.MAINTENANCE
                : ActivityCategory.CONFIGURATION;
        AuditAction auditAction = normalizedKey.startsWith("maintenance.")
                ? AuditAction.UPDATE_MAINTENANCE_STATUS
                : AuditAction.UPDATE_CONFIGURATION;
        recordActivity(
                actor.user(),
                category,
                auditAction.name(),
                "SYSTEM_CONFIGURATION",
                normalizedKey,
                "System configuration updated",
                request.getReason()
        );
        saveAudit(
                actor.user(),
                normalizedKey.startsWith("maintenance.")
                        ? AuditEntityName.MAINTENANCE
                        : AuditEntityName.SYSTEM_CONFIGURATION,
                normalizedKey,
                auditAction,
                oldValue,
                savedConfiguration.getConfigValue(),
                request.getReason()
        );

        return toConfigurationResponse(savedConfiguration);
    }

    @Override
    @Transactional
    public SystemAnnouncementResponse broadcastAnnouncement(CurrentUser currentUser, CreateAnnouncementRequest request) {
        ActorContext actor = actorContext(currentUser);
        validateAnnouncementRequest(request);
        if (!policyEnabled(ANNOUNCEMENT_POLICY_KEY)) {
            throw AdministrationSupportException.configurationUpdateNotAllowed(
                    ANNOUNCEMENT_POLICY_KEY,
                    "announcement policy is not approved"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startsAt = request.getStartsAt() == null ? now : request.getStartsAt();
        AnnouncementStatus status = Boolean.TRUE.equals(request.getPublishNow()) || !startsAt.isAfter(now)
                ? AnnouncementStatus.ACTIVE
                : AnnouncementStatus.SCHEDULED;
        SystemAnnouncement announcement = SystemAnnouncement.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .audience(request.getAudience())
                .startsAt(startsAt)
                .endsAt(request.getEndsAt())
                .status(status)
                .publishedAt(status == AnnouncementStatus.ACTIVE ? now : null)
                .publishedBy(actor.user())
                .build();

        SystemAnnouncement savedAnnouncement = systemAnnouncementRepository.save(announcement);
        recordActivity(
                actor.user(),
                ActivityCategory.ANNOUNCEMENT,
                AuditAction.BROADCAST_ANNOUNCEMENT.name(),
                "ANNOUNCEMENT",
                savedAnnouncement.getId().toString(),
                "System announcement broadcast",
                savedAnnouncement.getTitle()
        );
        saveAudit(
                actor.user(),
                AuditEntityName.ANNOUNCEMENT,
                savedAnnouncement.getId().toString(),
                AuditAction.BROADCAST_ANNOUNCEMENT,
                null,
                savedAnnouncement.getTitle(),
                "System announcement broadcast"
        );

        return toAnnouncementResponse(savedAnnouncement);
    }

    @Override
    public Page<SystemAnnouncementResponse> searchAnnouncements(
            CurrentUser currentUser,
            AnnouncementStatus status,
            AnnouncementAudience audience,
            OffsetDateTime startsFrom,
            OffsetDateTime startsTo,
            String keyword,
            Pageable pageable
    ) {
        ActorContext actor = actorContext(currentUser);
        Set<AnnouncementAudience> visibleAudiences = actor.visibleAnnouncementAudiences();
        Pageable normalizedPageable = PageableLimits.normalize(pageable);
        if (audience != null && !visibleAudiences.contains(audience)) {
            return Page.empty(normalizedPageable);
        }
        validatePeriod(startsFrom, startsTo);
        return systemAnnouncementRepository.searchVisible(
                status,
                audience,
                visibleAudiences,
                startsFrom,
                startsTo,
                keyword(keyword),
                normalizedPageable
        ).map(this::toAnnouncementResponse);
    }

    @Override
    public SystemAnnouncementResponse getAnnouncement(CurrentUser currentUser, UUID announcementId) {
        ActorContext actor = actorContext(currentUser);
        SystemAnnouncement announcement = findAnnouncement(announcementId);
        assertCanViewAnnouncement(actor, announcement);
        return toAnnouncementResponse(announcement);
    }

    @Override
    public Page<AdministrationAuditLogResponse> getAnnouncementHistory(
            CurrentUser currentUser,
            UUID announcementId,
            Pageable pageable
    ) {
        ActorContext actor = actorContext(currentUser);
        SystemAnnouncement announcement = findAnnouncement(announcementId);
        assertCanViewAnnouncement(actor, announcement);
        return auditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(
                AuditEntityName.ANNOUNCEMENT,
                announcement.getId().toString(),
                PageableLimits.normalize(pageable)
        ).map(this::toAuditLogResponse);
    }

    @Override
    public MaintenanceStatusResponse maintenanceStatus() {
        return MaintenanceStatusResponse.builder()
                .policyEnabled(policyEnabled(MAINTENANCE_POLICY_KEY))
                .maintenanceMode(policyEnabled(MAINTENANCE_MODE_KEY))
                .message(configurationValue(MAINTENANCE_MESSAGE_KEY))
                .startsAt(configurationDateTimeValue(MAINTENANCE_WINDOW_STARTS_AT_KEY))
                .endsAt(configurationDateTimeValue(MAINTENANCE_WINDOW_ENDS_AT_KEY))
                .build();
    }

    @Override
    @Transactional
    public MaintenanceStatusResponse updateMaintenanceStatus(
            CurrentUser currentUser,
            UpdateMaintenanceStatusRequest request
    ) {
        ActorContext actor = actorContext(currentUser);
        validateMaintenanceUpdateRequest(request);

        Boolean requestedMaintenanceMode = requestedMaintenanceMode(request);
        OffsetDateTime nextStartsAt = request.getStartsAt() == null
                ? configurationDateTimeValue(MAINTENANCE_WINDOW_STARTS_AT_KEY)
                : request.getStartsAt();
        OffsetDateTime nextEndsAt = request.getEndsAt() == null
                ? configurationDateTimeValue(MAINTENANCE_WINDOW_ENDS_AT_KEY)
                : request.getEndsAt();
        validateMaintenanceWindow(nextStartsAt, nextEndsAt);

        Map<String, String> oldValues = new LinkedHashMap<>();
        Map<String, String> newValues = new LinkedHashMap<>();
        if (requestedMaintenanceMode != null) {
            updateMaintenanceConfiguration(
                    MAINTENANCE_MODE_KEY,
                    "Maintenance Mode Enabled",
                    "Current operational maintenance status.",
                    String.valueOf(requestedMaintenanceMode),
                    SystemConfigurationValueType.BOOLEAN,
                    true,
                    actor.user(),
                    request,
                    oldValues,
                    newValues
            );
        }
        if (request.getMessage() != null) {
            updateMaintenanceConfiguration(
                    MAINTENANCE_MESSAGE_KEY,
                    "Maintenance Message",
                    "Message displayed with maintenance status.",
                    request.getMessage(),
                    SystemConfigurationValueType.STRING,
                    false,
                    actor.user(),
                    request,
                    oldValues,
                    newValues
            );
        }
        if (request.getStartsAt() != null) {
            updateMaintenanceConfiguration(
                    MAINTENANCE_WINDOW_STARTS_AT_KEY,
                    "Maintenance Window Starts At",
                    "Scheduled maintenance window start time.",
                    request.getStartsAt().toString(),
                    SystemConfigurationValueType.STRING,
                    false,
                    actor.user(),
                    request,
                    oldValues,
                    newValues
            );
        }
        if (request.getEndsAt() != null) {
            updateMaintenanceConfiguration(
                    MAINTENANCE_WINDOW_ENDS_AT_KEY,
                    "Maintenance Window Ends At",
                    "Scheduled maintenance window end time.",
                    request.getEndsAt().toString(),
                    SystemConfigurationValueType.STRING,
                    false,
                    actor.user(),
                    request,
                    oldValues,
                    newValues
            );
        }

        if (!oldValues.isEmpty()) {
            recordActivity(
                    actor.user(),
                    ActivityCategory.MAINTENANCE,
                    AuditAction.UPDATE_MAINTENANCE_STATUS.name(),
                    "MAINTENANCE",
                    "maintenance-status",
                    "Maintenance status updated",
                    request.getReason()
            );
            saveAudit(
                    actor.user(),
                    AuditEntityName.MAINTENANCE,
                    "maintenance-status",
                    AuditAction.UPDATE_MAINTENANCE_STATUS,
                    maintenanceSnapshot(oldValues),
                    maintenanceSnapshot(newValues),
                    request.getReason()
            );
        }

        return maintenanceStatus();
    }

    @Override
    public AdministrationDashboardResponse dashboard(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        validatePeriod(periodStart, periodEnd);

        return AdministrationDashboardResponse.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .ticketsByStatus(toEnumCountMap(SupportTicketStatus.class, supportTicketRepository.countByStatus(periodStart, periodEnd)))
                .ticketsByPriority(toEnumCountMap(SupportTicketPriority.class, supportTicketRepository.countByPriority(periodStart, periodEnd)))
                .ticketsByCategory(toEnumCountMap(SupportTicketCategory.class, supportTicketRepository.countByCategory(periodStart, periodEnd)))
                .openTicketCount(supportTicketRepository.countByStatusIn(OPEN_STATUSES))
                .unassignedTicketCount(supportTicketRepository.countByAssigneeIsNullAndStatusIn(OPEN_STATUSES))
                .accountsByStatus(toEnumCountMap(AccountStatus.class, userRepository.countByStatus()))
                .activityByCategory(toEnumCountMap(ActivityCategory.class, activityLogRepository.countByCategory(periodStart, periodEnd)))
                .auditByAction(toEnumCountMap(AuditAction.class, auditLogRepository.countByAction(periodStart, periodEnd)))
                .roleCount(roleRepository.count())
                .permissionCount(permissionRepository.count())
                .maintenanceStatus(maintenanceStatus())
                .build();
    }

    @Override
    public AdministrationReportResponse report(
            AdministrationReportType reportType,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {
        if (reportType == null) {
            throw AdministrationSupportException.validation("Administration report type is required");
        }
        validatePeriod(periodStart, periodEnd);

        return AdministrationReportResponse.builder()
                .reportType(reportType.name())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedAt(OffsetDateTime.now())
                .metrics(reportMetrics(reportType, periodStart, periodEnd))
                .build();
    }

    private Map<String, Long> reportMetrics(
            AdministrationReportType reportType,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {
        return switch (reportType) {
            case TICKETS -> ticketReportMetrics(periodStart, periodEnd);
            case SUPPORT_PERFORMANCE -> supportPerformanceReportMetrics(periodStart, periodEnd);
            case USER_ACTIVITY -> userActivityReportMetrics(periodStart, periodEnd);
            case ROLE_ASSIGNMENTS -> auditActionReportMetrics(
                    List.of(AuditEntityName.USER_ROLE),
                    List.of(AuditAction.ASSIGN_ROLE, AuditAction.REVOKE_ROLE),
                    periodStart,
                    periodEnd
            );
            case PERMISSION_CHANGES -> auditActionReportMetrics(
                    List.of(AuditEntityName.PERMISSION, AuditEntityName.ROLE_PERMISSION),
                    List.of(
                            AuditAction.CREATE_PERMISSION,
                            AuditAction.UPDATE_PERMISSION,
                            AuditAction.DELETE_PERMISSION,
                            AuditAction.ASSIGN_PERMISSION,
                            AuditAction.ASSIGN_ROLE_PERMISSIONS,
                            AuditAction.REVOKE_PERMISSION,
                            AuditAction.REMOVE_PERMISSION
                    ),
                    periodStart,
                    periodEnd
            );
            case CONFIGURATION_CHANGES -> auditActionReportMetrics(
                    List.of(AuditEntityName.SYSTEM_CONFIGURATION),
                    List.of(AuditAction.UPDATE_CONFIGURATION),
                    periodStart,
                    periodEnd
            );
            case ANNOUNCEMENTS -> announcementReportMetrics(periodStart, periodEnd);
            case MAINTENANCE -> maintenanceReportMetrics(periodStart, periodEnd);
        };
    }

    private Map<String, Long> ticketReportMetrics(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        Map<String, Long> metrics = new LinkedHashMap<>();
        List<Object[]> statusRows = supportTicketRepository.countByStatus(periodStart, periodEnd);
        putEnumCounts(metrics, "status", SupportTicketStatus.class, statusRows);
        putEnumCounts(metrics, "priority", SupportTicketPriority.class, supportTicketRepository.countByPriority(periodStart, periodEnd));
        putEnumCounts(metrics, "category", SupportTicketCategory.class, supportTicketRepository.countByCategory(periodStart, periodEnd));
        metrics.put("total", sumRows(statusRows));
        metrics.put("open.current", supportTicketRepository.countByStatusIn(OPEN_STATUSES));
        metrics.put("unassigned.current", supportTicketRepository.countByAssigneeIsNullAndStatusIn(OPEN_STATUSES));
        return metrics;
    }

    private Map<String, Long> supportPerformanceReportMetrics(
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {
        Map<String, Long> metrics = auditActionReportMetrics(
                List.of(AuditEntityName.SUPPORT_TICKET),
                List.of(
                        AuditAction.ASSIGN_SUPPORT_TICKET,
                        AuditAction.RESOLVE_SUPPORT_TICKET,
                        AuditAction.CLOSE_SUPPORT_TICKET,
                        AuditAction.REOPEN_SUPPORT_TICKET,
                        AuditAction.UPDATE_SUPPORT_TICKET
                ),
                periodStart,
                periodEnd
        );
        metrics.put("open.current", supportTicketRepository.countByStatusIn(OPEN_STATUSES));
        metrics.put("unassigned.current", supportTicketRepository.countByAssigneeIsNullAndStatusIn(OPEN_STATUSES));
        return metrics;
    }

    private Map<String, Long> userActivityReportMetrics(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        Map<String, Long> metrics = new LinkedHashMap<>();
        List<Object[]> categoryRows = activityLogRepository.countByCategory(periodStart, periodEnd);
        putEnumCounts(metrics, "category", ActivityCategory.class, categoryRows);
        metrics.put("total", sumRows(categoryRows));
        return metrics;
    }

    private Map<String, Long> announcementReportMetrics(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        Map<String, Long> metrics = auditActionReportMetrics(
                List.of(AuditEntityName.ANNOUNCEMENT),
                List.of(AuditAction.BROADCAST_ANNOUNCEMENT),
                periodStart,
                periodEnd
        );
        metrics.put("audit.total", metrics.remove("total"));
        List<Object[]> statusRows = systemAnnouncementRepository.countByStatus(periodStart, periodEnd);
        putEnumCounts(metrics, "status", AnnouncementStatus.class, statusRows);
        putEnumCounts(metrics, "audience", AnnouncementAudience.class, systemAnnouncementRepository.countByAudience(periodStart, periodEnd));
        metrics.put("total", sumRows(statusRows));
        return metrics;
    }

    private Map<String, Long> maintenanceReportMetrics(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        Map<String, Long> metrics = auditActionReportMetrics(
                List.of(AuditEntityName.MAINTENANCE),
                List.of(AuditAction.UPDATE_MAINTENANCE_STATUS),
                periodStart,
                periodEnd
        );
        metrics.put("policy.enabled", policyEnabled(MAINTENANCE_POLICY_KEY) ? 1L : 0L);
        metrics.put("mode.enabled", policyEnabled(MAINTENANCE_MODE_KEY) ? 1L : 0L);
        return metrics;
    }

    private Map<String, Long> auditActionReportMetrics(
            Collection<AuditEntityName> entityNames,
            List<AuditAction> actions,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {
        Map<String, Long> metrics = new LinkedHashMap<>();
        List<Object[]> actionRows = auditLogRepository.countByActionForEntities(
                entityNames,
                actions,
                periodStart,
                periodEnd
        );
        putEnumCounts(metrics, "action", actions, actionRows);
        metrics.put("total", sumRows(actionRows));
        return metrics;
    }

    private <E extends Enum<E>> void putEnumCounts(
            Map<String, Long> metrics,
            String prefix,
            Class<E> enumType,
            List<Object[]> rows
    ) {
        putEnumCounts(metrics, prefix, List.of(enumType.getEnumConstants()), rows);
    }

    private <E extends Enum<E>> void putEnumCounts(
            Map<String, Long> metrics,
            String prefix,
            Collection<E> expectedValues,
            List<Object[]> rows
    ) {
        expectedValues.forEach(value -> metrics.put(prefix + "." + value.name(), 0L));
        for (Object[] row : rows) {
            if (row.length >= 2 && row[0] instanceof Enum<?> enumValue && row[1] instanceof Number number
                    && expectedValues.contains(row[0])) {
                metrics.put(prefix + "." + enumValue.name(), number.longValue());
            }
        }
    }

    private long sumRows(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row.length >= 2 && row[1] instanceof Number)
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
    }

    private SupportTicketResponse saveTicketMutation(
            SupportTicket ticket,
            User actor,
            TicketHistoryAction action,
            SupportTicketStatus previousStatus,
            UUID previousAssigneeId,
            String reason,
            String oldValue,
            String newValue
    ) {
        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        recordTicketHistory(
                savedTicket,
                actor,
                action,
                previousStatus,
                savedTicket.getStatus(),
                previousAssigneeId,
                userId(savedTicket.getAssignee()),
                null,
                reason,
                oldValue,
                newValue
        );
        recordTicketActivity(actor, savedTicket, action.name(), "Support ticket " + action.name().toLowerCase(Locale.ROOT));
        saveTicketAudit(actor, savedTicket, action, oldValue, newValue, reason);

        return toTicketResponse(savedTicket);
    }

    private void saveTicketAudit(
            User actor,
            SupportTicket ticket,
            TicketHistoryAction action,
            String oldValue,
            String newValue,
            String details
    ) {
        AuditAction auditAction = switch (action) {
            case ASSIGNED -> AuditAction.ASSIGN_SUPPORT_TICKET;
            case RESOLVED -> AuditAction.RESOLVE_SUPPORT_TICKET;
            case CLOSED -> AuditAction.CLOSE_SUPPORT_TICKET;
            case REOPENED -> AuditAction.REOPEN_SUPPORT_TICKET;
            default -> AuditAction.UPDATE_SUPPORT_TICKET;
        };
        saveAudit(
                actor,
                AuditEntityName.SUPPORT_TICKET,
                ticket.getId().toString(),
                auditAction,
                oldValue,
                newValue,
                details == null ? ticket.getTicketNumber() : details
        );
    }

    private SupportTicket findTicket(UUID ticketId) {
        if (ticketId == null) {
            throw AdministrationSupportException.validation("Support ticket id is required");
        }

        return supportTicketRepository.findDetailById(ticketId)
                .orElseThrow(() -> AdministrationSupportException.ticketNotFound(ticketId));
    }

    private SupportTicket findTicketForUpdate(UUID ticketId) {
        if (ticketId == null) {
            throw AdministrationSupportException.validation("Support ticket id is required");
        }

        return supportTicketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> AdministrationSupportException.ticketNotFound(ticketId));
    }

    private SystemAnnouncement findAnnouncement(UUID announcementId) {
        if (announcementId == null) {
            throw AdministrationSupportException.validation("Announcement id is required");
        }

        return systemAnnouncementRepository.findDetailById(announcementId)
                .orElseThrow(() -> AdministrationSupportException.announcementNotFound(announcementId));
    }

    private void assertCanViewAnnouncement(ActorContext actor, SystemAnnouncement announcement) {
        if (actor.visibleAnnouncementAudiences().contains(announcement.getAudience())) {
            return;
        }

        throw AdministrationSupportException.announcementNotFound(announcement.getId());
    }

    private ActorContext actorContext(CurrentUser currentUser) {
        User user = internalUserService.getCurrentUser(currentUser);
        Set<String> roles = userRepository.findRoleCodesByUserId(user.getId())
                .stream()
                .map(AdministrationSupportServiceImpl::normalize)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        return new ActorContext(user, roles);
    }

    private void assertCanViewTicket(ActorContext actor, SupportTicket ticket) {
        if (actor.canManageSupport() || Objects.equals(userId(ticket.getRequester()), actor.user().getId())) {
            return;
        }

        throw AdministrationSupportException.ticketScopeDenied(ticket.getId());
    }

    private void assertCanManageSupport(ActorContext actor, String action) {
        if (!actor.canManageSupport()) {
            throw AdministrationSupportException.operationForbidden(action);
        }
    }

    private void assertOpen(SupportTicket ticket, String action) {
        if (!OPEN_STATUSES.contains(ticket.getStatus())) {
            throw AdministrationSupportException.invalidTicketState(ticket.getId(), ticket.getStatus(), action);
        }
    }

    private void assertStaffUser(User assignee) {
        if (assignee.getStatus() != AccountStatus.ACTIVE) {
            throw AdministrationSupportException.validation("Assignee must be an active staff account");
        }
        Set<String> roles = userRepository.findRoleCodesByUserId(assignee.getId())
                .stream()
                .map(AdministrationSupportServiceImpl::normalize)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (roles.stream().noneMatch(STAFF_ROLE_CODES::contains)) {
            throw AdministrationSupportException.validation("Assignee must have a staff role");
        }
    }

    private void applyTicketUpdates(SupportTicket ticket, UpdateSupportTicketRequest request) {
        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
    }

    private void promoteInProgress(SupportTicket ticket) {
        if (ticket.getStatus() == SupportTicketStatus.RECEIVED
                || ticket.getStatus() == SupportTicketStatus.ASSIGNED
                || ticket.getStatus() == SupportTicketStatus.REOPENED) {
            ticket.setStatus(SupportTicketStatus.IN_PROGRESS);
            ticket.setLastStatusChangedAt(OffsetDateTime.now());
        }
    }

    private void recordTicketHistory(
            SupportTicket ticket,
            User actor,
            TicketHistoryAction action,
            SupportTicketStatus previousStatus,
            SupportTicketStatus newStatus,
            UUID previousAssigneeId,
            UUID newAssigneeId,
            String commentText,
            String reason,
            String oldValue,
            String newValue
    ) {
        SupportTicketHistory history = SupportTicketHistory.builder()
                .ticket(ticket)
                .actor(actor)
                .action(action)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .previousAssigneeId(previousAssigneeId)
                .newAssigneeId(newAssigneeId)
                .commentText(commentText)
                .reason(reason)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        supportTicketHistoryRepository.save(history);
    }

    private void recordTicketActivity(User actor, SupportTicket ticket, String action, String summary) {
        recordActivity(
                actor,
                ActivityCategory.SUPPORT_TICKET,
                action,
                "SUPPORT_TICKET",
                ticket.getId().toString(),
                summary,
                ticket.getTicketNumber()
        );
    }

    private void recordActivity(
            User actor,
            ActivityCategory category,
            String action,
            String entityType,
            String entityId,
            String summary,
            String details
    ) {
        ActivityLog activityLog = ActivityLog.builder()
                .actor(actor)
                .actorKeycloakId(actor == null ? null : actor.getKeycloakId())
                .actorUsername(actor == null ? null : actor.getUsername())
                .category(category)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .summary(summary)
                .details(details)
                .occurredAt(OffsetDateTime.now())
                .build();
        activityLogRepository.save(activityLog);
    }

    private void saveAudit(
            User actor,
            AuditEntityName entityName,
            String entityId,
            AuditAction action,
            String oldValue,
            String newValue,
            String details
    ) {
        auditLogService.saveAudit(new AuditLogCommand(
                entityName,
                entityId,
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getKeycloakId(),
                actor == null ? null : actor.getUsername(),
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getKeycloakId(),
                action,
                AuditStatus.SUCCESS,
                "system",
                "system",
                oldValue,
                newValue,
                details
        ));
    }

    private void validateConfigurationUpdate(
            SystemConfiguration configuration,
            UpdateSystemConfigurationRequest request
    ) {
        if (!Boolean.TRUE.equals(configuration.getEditable())) {
            throw AdministrationSupportException.configurationUpdateNotAllowed(
                    configuration.getConfigKey(),
                    "configuration is read-only"
            );
        }
        if (Boolean.TRUE.equals(configuration.getRequiresConfirmation())
                && !Boolean.TRUE.equals(request.getConfirmed())) {
            throw AdministrationSupportException.configurationUpdateNotAllowed(
                    configuration.getConfigKey(),
                    "confirmation is required"
            );
        }
        if (configuration.getConfigKey().startsWith("maintenance.")
                && !MAINTENANCE_POLICY_KEY.equals(configuration.getConfigKey())
                && !policyEnabled(MAINTENANCE_POLICY_KEY)) {
            throw AdministrationSupportException.configurationUpdateNotAllowed(
                    configuration.getConfigKey(),
                    "maintenance policy is not approved"
            );
        }
        validateValue(configuration.getValueType(), request.getValue());
    }

    private void validateValue(SystemConfigurationValueType valueType, String value) {
        try {
            switch (valueType) {
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(value.trim()) && !"false".equalsIgnoreCase(value.trim())) {
                        throw new IllegalArgumentException("boolean expected");
                    }
                }
                case INTEGER -> Long.parseLong(value.trim());
                case DECIMAL -> new BigDecimal(value.trim());
                case JSON -> objectMapper.readTree(value);
                case STRING -> {
                    if (blank(value)) {
                        throw new IllegalArgumentException("string expected");
                    }
                }
            }
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw AdministrationSupportException.validation("Configuration value does not match type " + valueType);
        }
    }

    private boolean policyEnabled(String configKey) {
        return systemConfigurationRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .map(value -> "true".equalsIgnoreCase(value.trim()))
                .orElse(false);
    }

    private String configurationValue(String configKey) {
        return systemConfigurationRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(null);
    }

    private OffsetDateTime configurationDateTimeValue(String configKey) {
        String value = configurationValue(configKey);
        if (blank(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private Boolean requestedMaintenanceMode(UpdateMaintenanceStatusRequest request) {
        return request.getMaintenanceMode() == null ? request.getEnabled() : request.getMaintenanceMode();
    }

    private void updateMaintenanceConfiguration(
            String configKey,
            String displayName,
            String description,
            String value,
            SystemConfigurationValueType valueType,
            boolean requiresConfirmation,
            User actor,
            UpdateMaintenanceStatusRequest request,
            Map<String, String> oldValues,
            Map<String, String> newValues
    ) {
        SystemConfiguration configuration = systemConfigurationRepository.findByConfigKey(configKey)
                .orElseGet(() -> SystemConfiguration.builder()
                        .configKey(configKey)
                        .displayName(displayName)
                        .description(description)
                        .configValue(value)
                        .valueType(valueType)
                        .editable(true)
                        .sensitive(false)
                        .requiresConfirmation(requiresConfirmation)
                        .build());
        UpdateSystemConfigurationRequest configurationRequest = new UpdateSystemConfigurationRequest();
        configurationRequest.setValue(value);
        configurationRequest.setReason(request.getReason());
        configurationRequest.setConfirmed(request.getConfirmed());
        validateConfigurationUpdate(configuration, configurationRequest);

        String oldValue = configuration.getId() == null ? null : configuration.getConfigValue();
        if (Objects.equals(oldValue, value)) {
            return;
        }

        configuration.setConfigValue(value);
        configuration.setUpdatedBy(actor);
        configuration.setLastChangeReason(request.getReason());
        systemConfigurationRepository.save(configuration);
        oldValues.put(configKey, oldValue);
        newValues.put(configKey, value);
    }

    private String maintenanceSnapshot(Map<String, String> values) {
        return values.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + value(entry.getValue()))
                .collect(Collectors.joining(";"));
    }

    private String nextTicketNumber() {
        String datePart = OffsetDateTime.now().format(TICKET_DATE_FORMAT);
        for (int attempt = 0; attempt < 10; attempt++) {
            String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            String ticketNumber = "SUP-" + datePart + "-" + suffix;
            if (!supportTicketRepository.existsByTicketNumber(ticketNumber)) {
                return ticketNumber;
            }
        }

        throw AdministrationSupportException.validation("Could not generate a unique support ticket number");
    }

    private void validateCreateTicketRequest(CreateSupportTicketRequest request) {
        if (request == null) {
            throw AdministrationSupportException.validation("Create ticket request is required");
        }
        if (blank(request.getTitle())) {
            throw AdministrationSupportException.validation("Ticket title is required");
        }
        if (blank(request.getDescription())) {
            throw AdministrationSupportException.validation("Ticket description is required");
        }
        if (request.getPriority() == null) {
            throw AdministrationSupportException.validation("Ticket priority is required");
        }
        if (request.getCategory() == null) {
            throw AdministrationSupportException.validation("Ticket category is required");
        }
    }

    private void validateUpdateTicketRequest(UpdateSupportTicketRequest request) {
        if (request == null) {
            throw AdministrationSupportException.validation("Update ticket request is required");
        }
        if (request.getTitle() != null && blank(request.getTitle())) {
            throw AdministrationSupportException.validation("Ticket title must not be blank");
        }
        if (request.getDescription() != null && blank(request.getDescription())) {
            throw AdministrationSupportException.validation("Ticket description must not be blank");
        }
    }

    private void validateConfigurationRequest(UpdateSystemConfigurationRequest request) {
        if (request == null) {
            throw AdministrationSupportException.validation("Configuration update request is required");
        }
        if (blank(request.getValue())) {
            throw AdministrationSupportException.validation("Configuration value is required");
        }
    }

    private void validateAnnouncementRequest(CreateAnnouncementRequest request) {
        if (request == null) {
            throw AdministrationSupportException.validation("Announcement request is required");
        }
        if (blank(request.getTitle())) {
            throw AdministrationSupportException.validation("Announcement title is required");
        }
        if (blank(request.getMessage())) {
            throw AdministrationSupportException.validation("Announcement message is required");
        }
        if (request.getAudience() == null) {
            throw AdministrationSupportException.validation("Announcement audience is required");
        }
        if (request.getEndsAt() != null
                && request.getStartsAt() != null
                && request.getEndsAt().isBefore(request.getStartsAt())) {
            throw AdministrationSupportException.validation("Announcement end time must be after start time");
        }
    }

    private void validateMaintenanceUpdateRequest(UpdateMaintenanceStatusRequest request) {
        if (request == null) {
            throw AdministrationSupportException.validation("Maintenance update request is required");
        }
        if (request.getMaintenanceMode() == null
                && request.getEnabled() == null
                && request.getMessage() == null
                && request.getStartsAt() == null
                && request.getEndsAt() == null) {
            throw AdministrationSupportException.validation("Maintenance update request must contain at least one change");
        }
        if (request.getMaintenanceMode() != null
                && request.getEnabled() != null
                && !Objects.equals(request.getMaintenanceMode(), request.getEnabled())) {
            throw AdministrationSupportException.validation("Maintenance mode and enabled values must match");
        }
        if (request.getMessage() != null && blank(request.getMessage())) {
            throw AdministrationSupportException.validation("Maintenance message must not be blank");
        }
        validateMaintenanceWindow(request.getStartsAt(), request.getEndsAt());
    }

    private void validateMaintenanceWindow(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw AdministrationSupportException.validation("Maintenance end time must be after or equal to start time");
        }
    }

    private void validatePeriod(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw AdministrationSupportException.validation("Period end must be after or equal to period start");
        }
    }

    private String requiredReason(TicketReasonRequest request, String message) {
        if (request == null || blank(request.getReason())) {
            throw AdministrationSupportException.validation(message);
        }

        return request.getReason().trim();
    }

    private String requireConfigKey(String configKey) {
        String normalized = normalize(configKey);
        if (normalized == null) {
            throw AdministrationSupportException.validation("Configuration key is required");
        }

        return normalized;
    }

    private SupportTicketResponse toTicketResponse(SupportTicket ticket) {
        User requester = ticket.getRequester();
        User assignee = ticket.getAssignee();
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .requesterId(userId(requester))
                .requesterEmail(requester == null ? null : requester.getEmail())
                .assigneeId(userId(assignee))
                .assigneeEmail(assignee == null ? null : assignee.getEmail())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .resolution(ticket.getResolution())
                .escalationReason(ticket.getEscalationReason())
                .receivedAt(ticket.getReceivedAt())
                .assignedAt(ticket.getAssignedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .reopenedAt(ticket.getReopenedAt())
                .lastStatusChangedAt(ticket.getLastStatusChangedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private SupportTicketHistoryResponse toTicketHistoryResponse(SupportTicketHistory history) {
        User actor = history.getActor();
        SupportTicket ticket = history.getTicket();
        return SupportTicketHistoryResponse.builder()
                .id(history.getId())
                .ticketId(ticket == null ? null : ticket.getId())
                .actorId(userId(actor))
                .actorEmail(actor == null ? null : actor.getEmail())
                .action(history.getAction())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .previousAssigneeId(history.getPreviousAssigneeId())
                .newAssigneeId(history.getNewAssigneeId())
                .commentText(history.getCommentText())
                .reason(history.getReason())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private ActivityLogResponse toActivityLogResponse(ActivityLog activityLog) {
        User actor = activityLog.getActor();
        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .actorId(userId(actor))
                .actorUsername(activityLog.getActorUsername())
                .category(activityLog.getCategory())
                .action(activityLog.getAction())
                .entityType(activityLog.getEntityType())
                .entityId(activityLog.getEntityId())
                .summary(activityLog.getSummary())
                .details(activityLog.getDetails())
                .occurredAt(activityLog.getOccurredAt())
                .createdAt(activityLog.getCreatedAt())
                .build();
    }

    private AdministrationAuditLogResponse toAuditLogResponse(AuditLog auditLog) {
        return AdministrationAuditLogResponse.builder()
                .id(auditLog.getId())
                .entityName(auditLog.getEntityName())
                .entityId(auditLog.getEntityId())
                .actorId(auditLog.getActorId())
                .actorUsername(auditLog.getActorUsername())
                .userId(auditLog.getUserId())
                .action(auditLog.getAction())
                .status(auditLog.getStatus())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .details(auditLog.getDetails())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private SystemConfigurationResponse toConfigurationResponse(SystemConfiguration configuration) {
        return SystemConfigurationResponse.builder()
                .id(configuration.getId())
                .configKey(configuration.getConfigKey())
                .displayName(configuration.getDisplayName())
                .description(configuration.getDescription())
                .value(Boolean.TRUE.equals(configuration.getSensitive()) ? "********" : configuration.getConfigValue())
                .valueType(configuration.getValueType())
                .sensitive(Boolean.TRUE.equals(configuration.getSensitive()))
                .editable(Boolean.TRUE.equals(configuration.getEditable()))
                .requiresConfirmation(Boolean.TRUE.equals(configuration.getRequiresConfirmation()))
                .updatedBy(userId(configuration.getUpdatedBy()))
                .lastChangeReason(configuration.getLastChangeReason())
                .createdAt(configuration.getCreatedAt())
                .updatedAt(configuration.getUpdatedAt())
                .build();
    }

    private SystemAnnouncementResponse toAnnouncementResponse(SystemAnnouncement announcement) {
        return SystemAnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .message(announcement.getMessage())
                .audience(announcement.getAudience())
                .status(announcement.getStatus())
                .startsAt(announcement.getStartsAt())
                .endsAt(announcement.getEndsAt())
                .publishedAt(announcement.getPublishedAt())
                .publishedBy(userId(announcement.getPublishedBy()))
                .cancelledAt(announcement.getCancelledAt())
                .cancellationReason(announcement.getCancellationReason())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setStatus(user.getStatus());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setLockReason(user.getLockReason());
        response.setLockedAt(user.getLockedAt());
        response.setLockedUntil(user.getLockedUntil());
        return response;
    }

    private <E extends Enum<E>> Map<E, Long> toEnumCountMap(Class<E> enumType, List<Object[]> rows) {
        Map<E, Long> counts = new EnumMap<>(enumType);
        for (E value : enumType.getEnumConstants()) {
            counts.put(value, 0L);
        }
        for (Object[] row : rows) {
            if (row.length >= 2 && enumType.isInstance(row[0]) && row[1] instanceof Number number) {
                counts.put(enumType.cast(row[0]), number.longValue());
            }
        }

        return counts;
    }

    private String ticketSnapshot(SupportTicket ticket) {
        return "title=" + ticket.getTitle()
                + ";priority=" + ticket.getPriority()
                + ";category=" + ticket.getCategory()
                + ";status=" + ticket.getStatus();
    }

    private static UUID userId(User user) {
        return user == null ? null : user.getId();
    }

    private static String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String keyword(String keyword) {
        String normalized = normalize(keyword);
        return normalized == null ? null : "%" + normalized + "%";
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record ActorContext(User user, Set<String> roleCodes) {

        boolean isAdmin() {
            return roleCodes.contains("admin");
        }

        boolean canManageSupport() {
            return roleCodes.stream().anyMatch(STAFF_ROLE_CODES::contains);
        }

        Set<AnnouncementAudience> visibleAnnouncementAudiences() {
            if (isAdmin()) {
                return Set.of(
                        AnnouncementAudience.ALL_USERS,
                        AnnouncementAudience.STAFF,
                        AnnouncementAudience.ADMINS
                );
            }
            if (canManageSupport()) {
                return Set.of(AnnouncementAudience.ALL_USERS, AnnouncementAudience.STAFF);
            }

            return Set.of(AnnouncementAudience.ALL_USERS);
        }
    }
}
