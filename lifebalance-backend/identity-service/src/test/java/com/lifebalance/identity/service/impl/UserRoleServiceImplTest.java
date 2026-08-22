package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.exception.UserRoleAssignmentException;
import java.util.Optional;
import java.util.UUID;

import com.lifebalance.identity.audit.AuditActor;
import com.lifebalance.identity.audit.AuditRequestMetadata;
import com.lifebalance.identity.audit.CurrentAuditActorResolver;
import com.lifebalance.identity.audit.CurrentAuditRequestMetadataResolver;
import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.repository.UserRoleRepository;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserAuthorizationCacheService userAuthorizationCacheService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentAuditActorResolver currentAuditActorResolver;

    @Mock
    private CurrentAuditRequestMetadataResolver currentAuditRequestMetadataResolver;

    @Test
    void shouldEvictAuthorizationAndAuditWhenAssigningRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User user = user(userId, "kc-user", "alice");
        Role role = role(roleId, "staff");
        User actor = user(actorId, "kc-admin", "admin");
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRoleId(roleId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);
        when(userRepository.findByKeycloakId("kc-admin")).thenReturn(Optional.of(actor));
        when(currentAuditActorResolver.resolve()).thenReturn(new AuditActor(null, null, null));
        when(currentAuditRequestMetadataResolver.resolve()).thenReturn(new AuditRequestMetadata("198.51.100.10", "JUnit"));

        createService().assignRole(userId, request, "kc-admin");

        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        ArgumentCaptor<AuditLogCommand> auditCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        verify(userAuthorizationCacheService).evictUser(userId);
        verify(auditLogService).saveAudit(auditCaptor.capture());

        assertThat(userRoleCaptor.getValue().getUser()).isSameAs(user);
        assertThat(userRoleCaptor.getValue().getRole()).isSameAs(role);
        assertThat(userRoleCaptor.getValue().getAssignedAt()).isNotNull();
        assertThat(auditCaptor.getValue().entityName()).isEqualTo(AuditEntityName.USER_ROLE);
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.ASSIGN_ROLE);
        assertThat(auditCaptor.getValue().actorId()).isEqualTo(actorId);
        assertThat(auditCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(auditCaptor.getValue().newValue()).isEqualTo("staff");
    }

    @Test
    void shouldEvictAuthorizationAndAuditWhenRevokingRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User user = user(userId, "kc-user", "alice");
        Role role = role(roleId, "staff");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);
        when(currentAuditActorResolver.resolve()).thenReturn(new AuditActor(actorId, "kc-admin", "admin"));
        when(currentAuditRequestMetadataResolver.resolve()).thenReturn(new AuditRequestMetadata("198.51.100.11", "JUnit"));

        createService().removeRole(userId, roleId);

        ArgumentCaptor<AuditLogCommand> auditCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
        verify(userAuthorizationCacheService).evictUser(userId);
        verify(auditLogService).saveAudit(auditCaptor.capture());

        assertThat(auditCaptor.getValue().entityName()).isEqualTo(AuditEntityName.USER_ROLE);
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.REVOKE_ROLE);
        assertThat(auditCaptor.getValue().actorId()).isEqualTo(actorId);
        assertThat(auditCaptor.getValue().oldValue()).isEqualTo("staff");
    }

    @Test
    void shouldRejectAssigningRoleThatIsAlreadyAssignedWithoutAuditOrCacheEviction() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRoleId(roleId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "kc-user", "alice")));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role(roleId, "staff")));
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);

        assertThatThrownBy(() -> createService().assignRole(userId, request, "kc-admin"))
                .isInstanceOf(UserRoleAssignmentException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.ROLE_ALREADY_ASSIGNED_TO_USER);

        verify(userRoleRepository, never()).save(any());
        verify(userAuthorizationCacheService, never()).evictUser(any());
        verify(auditLogService, never()).saveAudit(any());
    }

    @Test
    void shouldRejectRevokingRoleThatIsNotAssignedWithoutAuditOrCacheEviction() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "kc-user", "alice")));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role(roleId, "staff")));
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);

        assertThatThrownBy(() -> createService().removeRole(userId, roleId))
                .isInstanceOf(UserRoleAssignmentException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.ROLE_NOT_ASSIGNED_TO_USER);

        verify(userRoleRepository, never()).deleteByUserIdAndRoleId(any(), any());
        verify(userAuthorizationCacheService, never()).evictUser(any());
        verify(auditLogService, never()).saveAudit(any());
    }

    private UserRoleServiceImpl createService() {
        return new UserRoleServiceImpl(
                userRepository,
                roleRepository,
                userRoleRepository,
                userAuthorizationCacheService,
                auditLogService,
                currentAuditActorResolver,
                currentAuditRequestMetadataResolver
        );
    }

    private static User user(UUID id, String keycloakId, String username) {
        User user = new User();
        user.setId(id);
        user.setKeycloakId(keycloakId);
        user.setUsername(username);
        return user;
    }

    private static Role role(UUID id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        return role;
    }
}
