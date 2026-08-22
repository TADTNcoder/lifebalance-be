package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.dto.AssignSupportTicketRequest;
import com.lifebalance.identity.dto.AdministrationReportResponse;
import com.lifebalance.identity.dto.CreateSupportTicketRequest;
import com.lifebalance.identity.dto.SupportTicketResponse;
import com.lifebalance.identity.dto.SystemAnnouncementResponse;
import com.lifebalance.identity.dto.UpdateMaintenanceStatusRequest;
import com.lifebalance.identity.dto.UpdateSystemConfigurationRequest;
import com.lifebalance.identity.exception.AdministrationSupportException;
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
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;

@ExtendWith(MockitoExtension.class)
class AdministrationSupportServiceImplTest {

    @Mock
    private InternalUserService internalUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private SupportTicketHistoryRepository supportTicketHistoryRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private SystemConfigurationRepository systemConfigurationRepository;

    @Mock
    private SystemAnnouncementRepository systemAnnouncementRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogService auditLogService;

    private AdministrationSupportServiceImpl service;
    private CurrentUser currentUser;
    private User actor;

    @BeforeEach
    void setUp() {
        service = new AdministrationSupportServiceImpl(
                internalUserService,
                userRepository,
                roleRepository,
                permissionRepository,
                supportTicketRepository,
                supportTicketHistoryRepository,
                activityLogRepository,
                systemConfigurationRepository,
                systemAnnouncementRepository,
                auditLogRepository,
                auditLogService,
                new ObjectMapper()
        );
        currentUser = new CurrentUser("kc-admin-1", "admin", "admin@example.com", List.of("admin"));
        actor = user(UUID.fromString("11111111-1111-1111-1111-111111111111"), "admin@example.com");
        actor.setKeycloakId("kc-admin-1");
    }

    @Test
    void createTicketRecordsHistoryActivityAndAudit() {
        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("admin"));
        when(supportTicketRepository.existsByTicketNumber(any())).thenReturn(false);
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> {
            SupportTicket ticket = invocation.getArgument(0);
            ticket.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            return ticket;
        });

        CreateSupportTicketRequest request = new CreateSupportTicketRequest();
        request.setTitle("Cannot access account");
        request.setDescription("I cannot sign in after password reset.");
        request.setPriority(SupportTicketPriority.HIGH);
        request.setCategory(SupportTicketCategory.ACCOUNT_ACCESS);

        SupportTicketResponse response = service.createTicket(currentUser, request);

        assertThat(response.status()).isEqualTo(SupportTicketStatus.NEW);
        assertThat(response.requesterId()).isEqualTo(actor.getId());
        assertThat(response.ticketNumber()).startsWith("SUP-");

        ArgumentCaptor<SupportTicketHistory> historyCaptor = ArgumentCaptor.forClass(SupportTicketHistory.class);
        verify(supportTicketHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getAction().name()).isEqualTo("CREATED");
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(SupportTicketStatus.NEW);

        ArgumentCaptor<ActivityLog> activityCaptor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(activityCaptor.capture());
        assertThat(activityCaptor.getValue().getCategory()).isEqualTo(ActivityCategory.SUPPORT_TICKET);
        assertThat(activityCaptor.getValue().getAction()).isEqualTo("CREATE_SUPPORT_TICKET");

        ArgumentCaptor<AuditLogCommand> auditCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(auditLogService).saveAudit(auditCaptor.capture());
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.CREATE_SUPPORT_TICKET);
    }

    @Test
    void searchTicketsRestrictsRegularUserToOwnTickets() {
        User regularUser = user(UUID.fromString("33333333-3333-3333-3333-333333333333"), "alice@example.com");
        CurrentUser userPrincipal = new CurrentUser("kc-user-1", "alice", "alice@example.com", List.of("user"));
        SupportTicket ticket = ticket(regularUser);

        when(internalUserService.getCurrentUser(userPrincipal)).thenReturn(regularUser);
        when(userRepository.findRoleCodesByUserId(regularUser.getId())).thenReturn(List.of("user"));
        when(supportTicketRepository.search(
                eq(regularUser.getId()),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(ticket)));

        Page<SupportTicketResponse> response = service.searchTickets(
                userPrincipal,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                "",
                Pageable.unpaged()
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().requesterId()).isEqualTo(regularUser.getId());
    }

    @Test
    void assignTicketRejectsAssigneeWithoutStaffRoleWithoutMutatingTicket() {
        UUID ticketId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        User assignee = user(UUID.fromString("55555555-5555-5555-5555-555555555555"), "user@example.com");
        SupportTicket ticket = ticket(actor);
        ticket.setId(ticketId);

        AssignSupportTicketRequest request = new AssignSupportTicketRequest();
        request.setAssigneeId(assignee.getId());

        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("admin"));
        when(supportTicketRepository.findByIdForUpdate(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(userRepository.findRoleCodesByUserId(assignee.getId())).thenReturn(List.of("user"));

        assertThatThrownBy(() -> service.assignTicket(currentUser, ticketId, request))
                .isInstanceOf(AdministrationSupportException.class)
                .hasMessage("Assignee must have a staff role");

        verify(supportTicketRepository, never()).save(any());
        verify(supportTicketHistoryRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void updateConfigurationRequiresConfirmationForSensitiveChange() {
        SystemConfiguration configuration = SystemConfiguration.builder()
                .id(UUID.randomUUID())
                .configKey("announcement.policy.enabled")
                .displayName("Announcement Policy Enabled")
                .configValue("false")
                .valueType(SystemConfigurationValueType.BOOLEAN)
                .editable(true)
                .sensitive(false)
                .requiresConfirmation(true)
                .build();
        UpdateSystemConfigurationRequest request = new UpdateSystemConfigurationRequest();
        request.setValue("true");
        request.setConfirmed(false);

        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("admin"));
        when(systemConfigurationRepository.findByConfigKey("announcement.policy.enabled"))
                .thenReturn(Optional.of(configuration));

        assertThatThrownBy(() -> service.updateConfiguration(currentUser, "announcement.policy.enabled", request))
                .isInstanceOf(AdministrationSupportException.class)
                .hasMessage("System configuration cannot be updated: announcement.policy.enabled");

        verify(systemConfigurationRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
        verify(auditLogService, never()).saveAudit(any(AuditLogCommand.class));
    }

    @Test
    void searchAnnouncementsRestrictsRegularUserToPublicAudience() {
        User regularUser = user(UUID.fromString("66666666-6666-6666-6666-666666666666"), "alice@example.com");
        CurrentUser userPrincipal = new CurrentUser("kc-user-1", "alice", "alice@example.com", List.of("user"));

        when(internalUserService.getCurrentUser(userPrincipal)).thenReturn(regularUser);
        when(userRepository.findRoleCodesByUserId(regularUser.getId())).thenReturn(List.of("user"));

        Page<SystemAnnouncementResponse> response = service.searchAnnouncements(
                userPrincipal,
                AnnouncementStatus.ACTIVE,
                AnnouncementAudience.STAFF,
                null,
                null,
                "",
                Pageable.unpaged()
        );

        assertThat(response.getContent()).isEmpty();
        verify(systemAnnouncementRepository, never()).searchVisible(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchAnnouncementsAllowsStaffAudienceForManager() {
        SystemAnnouncement announcement = SystemAnnouncement.builder()
                .id(UUID.fromString("77777777-7777-7777-7777-777777777777"))
                .title("Staff notice")
                .message("Internal maintenance")
                .audience(AnnouncementAudience.STAFF)
                .status(AnnouncementStatus.ACTIVE)
                .startsAt(OffsetDateTime.parse("2026-08-21T07:00:00Z"))
                .publishedBy(actor)
                .build();

        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("manager"));
        when(systemAnnouncementRepository.searchVisible(
                eq(AnnouncementStatus.ACTIVE),
                eq(AnnouncementAudience.STAFF),
                eq(Set.of(AnnouncementAudience.ALL_USERS, AnnouncementAudience.STAFF)),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(announcement)));

        Page<SystemAnnouncementResponse> response = service.searchAnnouncements(
                currentUser,
                AnnouncementStatus.ACTIVE,
                AnnouncementAudience.STAFF,
                null,
                null,
                "",
                Pageable.unpaged()
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().audience()).isEqualTo(AnnouncementAudience.STAFF);
    }

    @Test
    void getAnnouncementReturnsVisibleAnnouncement() {
        UUID announcementId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        SystemAnnouncement announcement = SystemAnnouncement.builder()
                .id(announcementId)
                .title("Public notice")
                .message("Visible to users")
                .audience(AnnouncementAudience.ALL_USERS)
                .status(AnnouncementStatus.ACTIVE)
                .startsAt(OffsetDateTime.parse("2026-08-21T07:00:00Z"))
                .publishedBy(actor)
                .build();

        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("admin"));
        when(systemAnnouncementRepository.findDetailById(announcementId)).thenReturn(Optional.of(announcement));

        SystemAnnouncementResponse response = service.getAnnouncement(currentUser, announcementId);

        assertThat(response.id()).isEqualTo(announcementId);
        assertThat(response.title()).isEqualTo("Public notice");
        assertThat(response.audience()).isEqualTo(AnnouncementAudience.ALL_USERS);
    }

    @Test
    void getAnnouncementHistoryChecksVisibilityAndMapsAuditEntries() {
        UUID announcementId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        SystemAnnouncement announcement = SystemAnnouncement.builder()
                .id(announcementId)
                .title("Admin notice")
                .message("Visible to admins")
                .audience(AnnouncementAudience.ADMINS)
                .status(AnnouncementStatus.ACTIVE)
                .startsAt(OffsetDateTime.parse("2026-08-21T07:00:00Z"))
                .publishedBy(actor)
                .build();
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .entityName(AuditEntityName.ANNOUNCEMENT)
                .entityId(announcementId.toString())
                .action(AuditAction.BROADCAST_ANNOUNCEMENT)
                .status(AuditStatus.SUCCESS)
                .details("System announcement broadcast")
                .build();

        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("admin"));
        when(systemAnnouncementRepository.findDetailById(announcementId)).thenReturn(Optional.of(announcement));
        when(auditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(
                eq(AuditEntityName.ANNOUNCEMENT),
                eq(announcementId.toString()),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(auditLog)));

        var response = service.getAnnouncementHistory(currentUser, announcementId, Pageable.unpaged());

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().action()).isEqualTo(AuditAction.BROADCAST_ANNOUNCEMENT);
    }

    @Test
    void updateMaintenanceStatusUpdatesConfigurationsAndRecordsAudit() {
        OffsetDateTime startsAt = OffsetDateTime.parse("2026-08-22T01:00:00Z");
        OffsetDateTime endsAt = OffsetDateTime.parse("2026-08-22T02:00:00Z");
        Map<String, SystemConfiguration> configurations = new HashMap<>();
        configurations.put("maintenance.policy.enabled", configuration("maintenance.policy.enabled", "true", SystemConfigurationValueType.BOOLEAN, true));
        configurations.put("maintenance.mode.enabled", configuration("maintenance.mode.enabled", "false", SystemConfigurationValueType.BOOLEAN, true));
        configurations.put("maintenance.message", configuration("maintenance.message", "LifeBalance is operating normally.", SystemConfigurationValueType.STRING, false));

        UpdateMaintenanceStatusRequest request = new UpdateMaintenanceStatusRequest();
        request.setEnabled(true);
        request.setMessage("Scheduled maintenance");
        request.setStartsAt(startsAt);
        request.setEndsAt(endsAt);
        request.setReason("Database upgrade");
        request.setConfirmed(true);

        when(internalUserService.getCurrentUser(currentUser)).thenReturn(actor);
        when(userRepository.findRoleCodesByUserId(actor.getId())).thenReturn(List.of("admin"));
        when(systemConfigurationRepository.findByConfigKey(any()))
                .thenAnswer(invocation -> Optional.ofNullable(configurations.get(invocation.getArgument(0))));
        when(systemConfigurationRepository.save(any(SystemConfiguration.class))).thenAnswer(invocation -> {
            SystemConfiguration configuration = invocation.getArgument(0);
            if (configuration.getId() == null) {
                configuration.setId(UUID.randomUUID());
            }
            configurations.put(configuration.getConfigKey(), configuration);
            return configuration;
        });

        var response = service.updateMaintenanceStatus(currentUser, request);

        assertThat(response.policyEnabled()).isTrue();
        assertThat(response.maintenanceMode()).isTrue();
        assertThat(response.message()).isEqualTo("Scheduled maintenance");
        assertThat(response.startsAt()).isEqualTo(startsAt);
        assertThat(response.endsAt()).isEqualTo(endsAt);

        ArgumentCaptor<ActivityLog> activityCaptor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(activityCaptor.capture());
        assertThat(activityCaptor.getValue().getCategory()).isEqualTo(ActivityCategory.MAINTENANCE);
        assertThat(activityCaptor.getValue().getAction()).isEqualTo("UPDATE_MAINTENANCE_STATUS");

        ArgumentCaptor<AuditLogCommand> auditCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(auditLogService).saveAudit(auditCaptor.capture());
        assertThat(auditCaptor.getValue().entityName()).isEqualTo(AuditEntityName.MAINTENANCE);
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.UPDATE_MAINTENANCE_STATUS);
        assertThat(auditCaptor.getValue().oldValue()).contains("maintenance.mode.enabled=false");
        assertThat(auditCaptor.getValue().newValue()).contains("maintenance.mode.enabled=true");
    }

    @Test
    void ticketReportIncludesGroupedTicketMetrics() {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");

        when(supportTicketRepository.countByStatus(periodStart, periodEnd))
                .thenReturn(List.<Object[]>of(new Object[] { SupportTicketStatus.NEW, 2L }));
        when(supportTicketRepository.countByPriority(periodStart, periodEnd))
                .thenReturn(List.<Object[]>of(new Object[] { SupportTicketPriority.HIGH, 1L }));
        when(supportTicketRepository.countByCategory(periodStart, periodEnd))
                .thenReturn(List.<Object[]>of(new Object[] { SupportTicketCategory.ACCOUNT_ACCESS, 2L }));
        when(supportTicketRepository.countByStatusIn(openStatusesForTest())).thenReturn(2L);
        when(supportTicketRepository.countByAssigneeIsNullAndStatusIn(openStatusesForTest())).thenReturn(1L);

        AdministrationReportResponse response = service.report(
                AdministrationReportType.TICKETS,
                periodStart,
                periodEnd
        );

        assertThat(response.reportType()).isEqualTo("TICKETS");
        assertThat(response.metrics()).containsEntry("status.NEW", 2L);
        assertThat(response.metrics()).containsEntry("priority.HIGH", 1L);
        assertThat(response.metrics()).containsEntry("category.ACCOUNT_ACCESS", 2L);
        assertThat(response.metrics()).containsEntry("total", 2L);
        assertThat(response.metrics()).containsEntry("open.current", 2L);
        assertThat(response.metrics()).containsEntry("unassigned.current", 1L);
    }

    @Test
    void roleAssignmentReportIncludesAuditActionMetrics() {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");

        when(auditLogRepository.countByActionForEntities(
                eq(List.of(AuditEntityName.USER_ROLE)),
                eq(List.of(AuditAction.ASSIGN_ROLE, AuditAction.REVOKE_ROLE)),
                eq(periodStart),
                eq(periodEnd)
        )).thenReturn(List.<Object[]>of(new Object[] { AuditAction.ASSIGN_ROLE, 3L }));

        AdministrationReportResponse response = service.report(
                AdministrationReportType.ROLE_ASSIGNMENTS,
                periodStart,
                periodEnd
        );

        assertThat(response.metrics()).containsEntry("action.ASSIGN_ROLE", 3L);
        assertThat(response.metrics()).containsEntry("action.REVOKE_ROLE", 0L);
        assertThat(response.metrics()).containsEntry("total", 3L);
    }

    @Test
    void auditReportIncludesActionAndEntityMetrics() {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");

        when(auditLogRepository.countByAction(periodStart, periodEnd))
                .thenReturn(List.<Object[]>of(new Object[] { AuditAction.UPDATE_USER, 2L }));
        when(auditLogRepository.countByEntityName(periodStart, periodEnd))
                .thenReturn(List.<Object[]>of(new Object[] { AuditEntityName.USER, 2L }));

        AdministrationReportResponse response = service.report(
                AdministrationReportType.AUDIT,
                periodStart,
                periodEnd
        );

        assertThat(response.reportType()).isEqualTo("AUDIT");
        assertThat(response.metrics()).containsEntry("action.UPDATE_USER", 2L);
        assertThat(response.metrics()).containsEntry("entity.USER", 2L);
        assertThat(response.metrics()).containsEntry("total", 2L);
    }

    @Test
    void systemOperationReportIncludesOperationalActivityAndAuditMetrics() {
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime periodEnd = OffsetDateTime.parse("2026-08-22T00:00:00Z");

        when(activityLogRepository.countByCategory(periodStart, periodEnd))
                .thenReturn(List.<Object[]>of(
                        new Object[] { ActivityCategory.CONFIGURATION, 1L },
                        new Object[] { ActivityCategory.MAINTENANCE, 2L },
                        new Object[] { ActivityCategory.SUPPORT_TICKET, 9L }
                ));
        when(auditLogRepository.countByActionForEntities(
                eq(List.of(AuditEntityName.SYSTEM_CONFIGURATION, AuditEntityName.MAINTENANCE)),
                eq(List.of(AuditAction.UPDATE_CONFIGURATION, AuditAction.UPDATE_MAINTENANCE_STATUS)),
                eq(periodStart),
                eq(periodEnd)
        )).thenReturn(List.<Object[]>of(new Object[] { AuditAction.UPDATE_MAINTENANCE_STATUS, 4L }));
        when(systemConfigurationRepository.findByConfigKey("maintenance.policy.enabled"))
                .thenReturn(Optional.of(configuration(
                        "maintenance.policy.enabled",
                        "true",
                        SystemConfigurationValueType.BOOLEAN,
                        false
                )));
        when(systemConfigurationRepository.findByConfigKey("maintenance.mode.enabled"))
                .thenReturn(Optional.of(configuration(
                        "maintenance.mode.enabled",
                        "false",
                        SystemConfigurationValueType.BOOLEAN,
                        false
                )));

        AdministrationReportResponse response = service.report(
                AdministrationReportType.SYSTEM_OPERATION,
                periodStart,
                periodEnd
        );

        assertThat(response.reportType()).isEqualTo("SYSTEM_OPERATION");
        assertThat(response.metrics()).containsEntry("activity.category.CONFIGURATION", 1L);
        assertThat(response.metrics()).containsEntry("activity.category.MAINTENANCE", 2L);
        assertThat(response.metrics()).containsEntry("activity.category.SYSTEM", 0L);
        assertThat(response.metrics()).containsEntry("activity.total", 3L);
        assertThat(response.metrics()).containsEntry("audit.action.UPDATE_MAINTENANCE_STATUS", 4L);
        assertThat(response.metrics()).containsEntry("audit.total", 4L);
        assertThat(response.metrics()).containsEntry("policy.enabled", 1L);
        assertThat(response.metrics()).containsEntry("mode.enabled", 0L);
        assertThat(response.metrics()).containsEntry("total", 7L);
    }

    private static SupportTicket ticket(User requester) {
        return SupportTicket.builder()
                .id(UUID.randomUUID())
                .ticketNumber("SUP-20260821-ABCDEF12")
                .requester(requester)
                .title("Cannot access account")
                .description("I cannot sign in.")
                .priority(SupportTicketPriority.HIGH)
                .category(SupportTicketCategory.ACCOUNT_ACCESS)
                .status(SupportTicketStatus.NEW)
                .build();
    }

    private static User user(UUID userId, String email) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf('@')));
        user.setStatus(AccountStatus.ACTIVE);
        return user;
    }

    private static SystemConfiguration configuration(
            String configKey,
            String configValue,
            SystemConfigurationValueType valueType,
            boolean requiresConfirmation
    ) {
        return SystemConfiguration.builder()
                .id(UUID.randomUUID())
                .configKey(configKey)
                .displayName(configKey)
                .configValue(configValue)
                .valueType(valueType)
                .editable(true)
                .sensitive(false)
                .requiresConfirmation(requiresConfirmation)
                .build();
    }

    private static Set<SupportTicketStatus> openStatusesForTest() {
        return Set.of(
                SupportTicketStatus.NEW,
                SupportTicketStatus.RECEIVED,
                SupportTicketStatus.ASSIGNED,
                SupportTicketStatus.IN_PROGRESS,
                SupportTicketStatus.ESCALATED,
                SupportTicketStatus.REOPENED
        );
    }
}
