package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.lifebalance.identity.audit.AuditActor;
import com.lifebalance.identity.audit.AuditRequestMetadata;
import com.lifebalance.identity.audit.CurrentAuditActorResolver;
import com.lifebalance.identity.audit.CurrentAuditRequestMetadataResolver;
import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.repository.RolePermissionRepository;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private UserAuthorizationCacheService userAuthorizationCacheService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentAuditActorResolver currentAuditActorResolver;

    @Mock
    private CurrentAuditRequestMetadataResolver currentAuditRequestMetadataResolver;

    @Test
    void shouldEvictAffectedUsersAndAuditWhenAssigningPermission() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Role role = role(roleId, "manager");
        Permission permission = permission(permissionId, "audit:read");
        AssignPermissionRequest request = new AssignPermissionRequest();
        request.setPermissionId(permissionId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(roleId, permissionId)).thenReturn(false);
        when(currentAuditActorResolver.resolve()).thenReturn(new AuditActor(actorId, "kc-admin", "admin"));
        when(currentAuditRequestMetadataResolver.resolve()).thenReturn(new AuditRequestMetadata("198.51.100.12", "JUnit"));

        createService().assignPermission(roleId, request);

        ArgumentCaptor<RolePermission> rolePermissionCaptor = ArgumentCaptor.forClass(RolePermission.class);
        ArgumentCaptor<AuditLogCommand> auditCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(rolePermissionRepository).save(rolePermissionCaptor.capture());
        verify(userAuthorizationCacheService).evictUsersByRoleId(roleId);
        verify(auditLogService).saveAudit(auditCaptor.capture());

        assertThat(rolePermissionCaptor.getValue().getRole()).isSameAs(role);
        assertThat(rolePermissionCaptor.getValue().getPermission()).isSameAs(permission);
        assertThat(auditCaptor.getValue().entityName()).isEqualTo(AuditEntityName.ROLE_PERMISSION);
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.ASSIGN_PERMISSION);
        assertThat(auditCaptor.getValue().actorId()).isEqualTo(actorId);
        assertThat(auditCaptor.getValue().newValue()).isEqualTo("audit:read");
    }

    @Test
    void shouldEvictAffectedUsersAndAuditWhenRevokingPermission() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Role role = role(roleId, "manager");
        Permission permission = permission(permissionId, "audit:read");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(roleId, permissionId)).thenReturn(true);
        when(currentAuditActorResolver.resolve()).thenReturn(new AuditActor(actorId, "kc-admin", "admin"));
        when(currentAuditRequestMetadataResolver.resolve()).thenReturn(new AuditRequestMetadata("198.51.100.13", "JUnit"));

        createService().removePermission(roleId, permissionId);

        ArgumentCaptor<AuditLogCommand> auditCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(rolePermissionRepository).deleteByIdRoleIdAndIdPermissionId(roleId, permissionId);
        verify(userAuthorizationCacheService).evictUsersByRoleId(roleId);
        verify(auditLogService).saveAudit(auditCaptor.capture());

        assertThat(auditCaptor.getValue().entityName()).isEqualTo(AuditEntityName.ROLE_PERMISSION);
        assertThat(auditCaptor.getValue().action()).isEqualTo(AuditAction.REVOKE_PERMISSION);
        assertThat(auditCaptor.getValue().actorId()).isEqualTo(actorId);
        assertThat(auditCaptor.getValue().oldValue()).isEqualTo("audit:read");
    }

    private RolePermissionServiceImpl createService() {
        return new RolePermissionServiceImpl(
                roleRepository,
                permissionRepository,
                rolePermissionRepository,
                userAuthorizationCacheService,
                auditLogService,
                currentAuditActorResolver,
                currentAuditRequestMetadataResolver
        );
    }

    private static Role role(UUID id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        return role;
    }

    private static Permission permission(UUID id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        return permission;
    }
}
