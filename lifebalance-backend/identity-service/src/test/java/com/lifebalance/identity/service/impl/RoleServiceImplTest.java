package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lifebalance.identity.audit.RoleAuditEventPublisher;
import com.lifebalance.identity.audit.RoleAuditSnapshotMapper;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.exception.PermissionNotFoundException;
import com.lifebalance.identity.exception.RoleAssignedToUserException;
import com.lifebalance.identity.exception.RoleNotFoundException;
import com.lifebalance.identity.exception.SystemRoleProtectedException;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.repository.RolePermissionRepository;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.RoleBusinessValidator;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private UserAuthorizationCacheService userAuthorizationCacheService;

    @Mock
    private RoleAuditSnapshotMapper roleAuditSnapshotMapper;

    @Mock
    private RoleAuditEventPublisher roleAuditEventPublisher;

    @Test
    void shouldCreateCustomRoleWithPermissions() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "task.read", "Task");
        CreateRoleRequest request = createRoleRequest(" Manager ", " Manager ", " Operational managers ");
        request.setPermissionIds(List.of(permissionId));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(UUID.randomUUID());
            return role;
        });
        when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));

        RoleServiceImpl service = createService();

        RoleResponse response = service.createRole(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        verify(rolePermissionRepository).deleteByRoleId(roleCaptor.getValue().getId());
        verify(rolePermissionRepository).saveAll(any());

        assertThat(roleCaptor.getValue().getCode()).isEqualTo("manager");
        assertThat(roleCaptor.getValue().getName()).isEqualTo("Manager");
        assertThat(roleCaptor.getValue().getDescription()).isEqualTo("Operational managers");
        assertThat(roleCaptor.getValue().getSystem()).isFalse();
        assertThat(response.getCode()).isEqualTo("manager");
        assertThat(response.getName()).isEqualTo("Manager");
        assertThat(response.getSystem()).isFalse();
        assertThat(response.getPermissions()).extracting("id").containsExactly(permissionId);
    }

    @Test
    void shouldPublishAuditEventWhenCreatingRole() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "task.read", "Task");
        CreateRoleRequest request = createRoleRequest(" Manager ", " Manager ", " Operational managers ");
        request.setPermissionIds(List.of(permissionId));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(roleId);
            return role;
        });
        when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));
        when(roleAuditSnapshotMapper.toJson(any(Role.class), eq(List.of(permission)))).thenReturn("new-role");

        RoleServiceImpl service = createService();

        service.createRole(request);

        verify(roleAuditEventPublisher).publishRoleAudit(
                eq(AuditAction.CREATE_ROLE),
                eq(roleId),
                isNull(),
                eq("new-role"),
                eq("Role created")
        );
    }

    @Test
    void shouldThrowWhenRoleIsNotFoundById() {
        UUID roleId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.getRoleById(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found: " + roleId);
    }

    @Test
    void shouldGetRoleByCodeWithPermissions() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission permission = createPermission(permissionId, "task.read", "Task");
        when(roleRepository.findByCode("manager")).thenReturn(Optional.of(role));
        when(permissionRepository.findByRoleId(roleId)).thenReturn(List.of(permission));

        RoleServiceImpl service = createService();

        RoleResponse response = service.getRoleByCode(" Manager ");

        assertThat(response.getId()).isEqualTo(roleId);
        assertThat(response.getPermissions()).extracting("code").containsExactly("task.read");
    }

    @Test
    void shouldGetAllRolesWithPermissions() {
        UUID managerRoleId = UUID.randomUUID();
        UUID userRoleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role managerRole = createRole(managerRoleId, false);
        Role userRole = createRole(userRoleId, false);
        userRole.setCode("user");
        Permission permission = createPermission(permissionId, "task.read", "Task");
        when(roleRepository.findAll()).thenReturn(List.of(userRole, managerRole));
        when(rolePermissionRepository.findByRoleIds(List.of(managerRoleId, userRoleId)))
                .thenReturn(List.of(createRolePermission(managerRole, permission)));

        RoleServiceImpl service = createService();

        List<RoleResponse> responses = service.getAllRoles();

        assertThat(responses).extracting(RoleResponse::getCode).containsExactly("manager", "user");
        assertThat(responses.getFirst().getPermissions()).extracting("id").containsExactly(permissionId);
        assertThat(responses.get(1).getPermissions()).isEmpty();
    }

    @Test
    void shouldUpdateCustomRoleAndReplacePermissions() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission permission = createPermission(permissionId, "task.write", "Task");
        UpdateRoleRequest request = updateRoleRequest(" Updated Manager ", " Updated description ");
        request.setPermissionIds(List.of(permissionId));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));

        RoleServiceImpl service = createService();

        RoleResponse response = service.updateRole(roleId, request);

        verify(roleRepository).save(role);
        verify(rolePermissionRepository).deleteByRoleId(roleId);
        verify(rolePermissionRepository).saveAll(any());
        assertThat(role.getName()).isEqualTo("Updated Manager");
        assertThat(role.getDescription()).isEqualTo("Updated description");
        assertThat(role.getSystem()).isFalse();
        assertThat(response.getName()).isEqualTo("Updated Manager");
        assertThat(response.getPermissions()).extracting("code").containsExactly("task.write");
    }

    @Test
    void shouldPublishAuditEventWhenUpdatingRole() {
        UUID roleId = UUID.randomUUID();
        UUID oldPermissionId = UUID.randomUUID();
        UUID newPermissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission oldPermission = createPermission(oldPermissionId, "task.read", "Task");
        Permission newPermission = createPermission(newPermissionId, "task.write", "Task");
        UpdateRoleRequest request = updateRoleRequest(" Updated Manager ", " Updated description ");
        request.setPermissionIds(List.of(newPermissionId));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByRoleId(roleId)).thenReturn(List.of(oldPermission));
        when(roleRepository.save(role)).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionRepository.findAllById(List.of(newPermissionId))).thenReturn(List.of(newPermission));
        when(roleAuditSnapshotMapper.toJson(any(Role.class), any()))
                .thenReturn("old-role", "new-role");

        RoleServiceImpl service = createService();

        service.updateRole(roleId, request);

        verify(roleAuditEventPublisher).publishRoleAudit(
                eq(AuditAction.UPDATE_ROLE),
                eq(roleId),
                eq("old-role"),
                eq("new-role"),
                eq("Role updated")
        );
    }

    @Test
    void shouldKeepPermissionsWhenUpdateRequestDoesNotContainPermissionIds() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission permission = createPermission(permissionId, "task.read", "Task");
        UpdateRoleRequest request = updateRoleRequest(" Updated Manager ", " Updated description ");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionRepository.findByRoleId(roleId)).thenReturn(List.of(permission));

        RoleServiceImpl service = createService();

        RoleResponse response = service.updateRole(roleId, request);

        verify(rolePermissionRepository, never()).deleteByRoleId(any());
        assertThat(response.getPermissions()).extracting("id").containsExactly(permissionId);
    }

    @Test
    void shouldRejectSystemRoleUpdate() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);
        UpdateRoleRequest request = updateRoleRequest("Administrator", "Protected");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.updateRole(roleId, request))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
        verify(roleRepository, never()).save(any());
        verifyNoInteractions(roleAuditEventPublisher);
    }

    @Test
    void shouldDeleteCustomRole() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        service.deleteRole(roleId);

        verify(roleRepository).delete(role);
    }

    @Test
    void shouldPublishAuditEventWhenDeletingRole() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission permission = createPermission(permissionId, "task.read", "Task");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByRoleId(roleId)).thenReturn(List.of(permission));
        when(roleAuditSnapshotMapper.toJson(role, List.of(permission))).thenReturn("old-role");

        RoleServiceImpl service = createService();

        service.deleteRole(roleId);

        verify(roleAuditEventPublisher).publishRoleAudit(
                eq(AuditAction.DELETE_ROLE),
                eq(roleId),
                eq("old-role"),
                isNull(),
                eq("Role deleted")
        );
    }

    @Test
    void shouldRejectSystemRoleDelete() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.deleteRole(roleId))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void shouldRejectDeleteWhenRoleIsAssignedToUser() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.existsAssignedToActiveUser(roleId)).thenReturn(true);

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.deleteRole(roleId))
                .isInstanceOf(RoleAssignedToUserException.class)
                .hasMessage("Role is assigned to at least one user: " + roleId);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void shouldAssignPermissionsToRole() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission permission = createPermission(permissionId, "finance.read", "Finance");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));

        RoleServiceImpl service = createService();

        RoleResponse response = service.assignPermissionsToRole(roleId, List.of(permissionId));

        verify(rolePermissionRepository).deleteByRoleId(roleId);
        verify(rolePermissionRepository).saveAll(any());
        assertThat(response.getPermissions()).extracting("id").containsExactly(permissionId);
    }

    @Test
    void shouldPublishAuditEventWhenAssigningRolePermissions() {
        UUID roleId = UUID.randomUUID();
        UUID oldPermissionId = UUID.randomUUID();
        UUID newPermissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        Permission oldPermission = createPermission(oldPermissionId, "task.read", "Task");
        Permission newPermission = createPermission(newPermissionId, "finance.read", "Finance");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByRoleId(roleId)).thenReturn(List.of(oldPermission));
        when(permissionRepository.findAllById(List.of(newPermissionId))).thenReturn(List.of(newPermission));
        when(roleAuditSnapshotMapper.toJson(any(Role.class), any()))
                .thenReturn("old-role", "new-role");

        RoleServiceImpl service = createService();

        service.assignPermissionsToRole(roleId, List.of(newPermissionId));

        verify(roleAuditEventPublisher).publishRoleAudit(
                eq(AuditAction.ASSIGN_ROLE_PERMISSIONS),
                eq(roleId),
                eq("old-role"),
                eq("new-role"),
                eq("Role permissions changed")
        );
    }

    @Test
    void shouldClearPermissionsWhenAssigningEmptyList() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        RoleResponse response = service.assignPermissionsToRole(roleId, List.of());

        verify(rolePermissionRepository).deleteByRoleId(roleId);
        verify(rolePermissionRepository, never()).saveAll(any());
        assertThat(response.getPermissions()).isEmpty();
    }

    @Test
    void shouldRejectMissingPermissionWhenAssigningPermissions() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of());

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.assignPermissionsToRole(roleId, List.of(permissionId)))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessage("Permission not found: " + permissionId);
        verify(rolePermissionRepository, never()).deleteByRoleId(any());
        verify(rolePermissionRepository, never()).saveAll(any());
    }

    @Test
    void shouldRejectSystemRolePermissionAssignment() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.assignPermissionsToRole(roleId, List.of(UUID.randomUUID())))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
        verify(rolePermissionRepository, never()).deleteByRoleId(any());
    }

    private RoleServiceImpl createService() {
        return new RoleServiceImpl(
                roleRepository,
                new RoleBusinessValidator(roleRepository),
                permissionRepository,
                rolePermissionRepository,
                userAuthorizationCacheService,
                roleAuditSnapshotMapper,
                roleAuditEventPublisher
        );
    }

    private static CreateRoleRequest createRoleRequest(
            String code,
            String name,
            String description
    ) {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode(code);
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    private static UpdateRoleRequest updateRoleRequest(String name, String description) {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    private static Role createRole(UUID roleId, boolean system) {
        Role role = new Role();
        role.setId(roleId);
        role.setCode("manager");
        role.setName("Manager");
        role.setDescription("Managers");
        role.setSystem(system);
        return role;
    }

    private static Permission createPermission(UUID permissionId, String code, String module) {
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setCode(code);
        permission.setName(code);
        permission.setModule(module);
        permission.setSystem(false);
        return permission;
    }

    private static RolePermission createRolePermission(Role role, Permission permission) {
        return RolePermission.builder()
                .id(new RolePermissionId(role.getId(), permission.getId()))
                .role(role)
                .permission(permission)
                .grantedAt(OffsetDateTime.now())
                .build();
    }
}
