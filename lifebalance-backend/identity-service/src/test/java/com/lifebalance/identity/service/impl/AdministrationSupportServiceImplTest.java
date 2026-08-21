package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
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
import com.lifebalance.identity.dto.CreateSupportTicketRequest;
import com.lifebalance.identity.dto.SupportTicketResponse;
import com.lifebalance.identity.dto.SystemAnnouncementResponse;
import com.lifebalance.identity.dto.UpdateSystemConfigurationRequest;
import com.lifebalance.identity.exception.AdministrationSupportException;
import com.lifebalance.identity.model.ActivityLog;
import com.lifebalance.identity.model.SupportTicket;
import com.lifebalance.identity.model.SupportTicketHistory;
import com.lifebalance.identity.model.SystemAnnouncement;
import com.lifebalance.identity.model.SystemConfiguration;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.model.enums.ActivityCategory;
import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;
import com.lifebalance.identity.model.enums.AuditAction;
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
}
