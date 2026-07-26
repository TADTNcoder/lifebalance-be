package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.exception.PermissionNotFoundException;
import com.lifebalance.identity.exception.PermissionValidationException;
import com.lifebalance.identity.exception.SystemPermissionProtectedException;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.service.PermissionBusinessValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Test
    void shouldGetAllPermissionsOrderedByModuleAndCode() {
        Permission taskPermission = createPermission(UUID.randomUUID(), "task:read", "Read Tasks", "task", false);
        Permission userPermission = createPermission(UUID.randomUUID(), "user:read", "Read Users", "user", false);
        when(permissionRepository.findAllByOrderByModuleAscCodeAsc())
                .thenReturn(List.of(taskPermission, userPermission));

        PermissionServiceImpl service = createService();

        List<PermissionResponse> responses = service.getAllPermissions();

        assertThat(responses)
                .extracting(PermissionResponse::getCode)
                .containsExactly("task:read", "user:read");
    }

    @Test
    void shouldGetPermissionsByModule() {
        Permission permission = createPermission(UUID.randomUUID(), "task:read", "Read Tasks", "task", false);
        when(permissionRepository.findByModule("task")).thenReturn(List.of(permission));

        PermissionServiceImpl service = createService();

        List<PermissionResponse> responses = service.getPermissionsByModule(" Task ");

        verify(permissionRepository).findByModule("task");
        assertThat(responses)
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.getCode()).isEqualTo("task:read");
                    assertThat(response.getModule()).isEqualTo("task");
                });
    }

    @Test
    void shouldThrowWhenModuleIsBlank() {
        PermissionServiceImpl service = createService();

        assertThatThrownBy(() -> service.getPermissionsByModule(" "))
                .isInstanceOf(PermissionValidationException.class)
                .hasMessage("Permission module is required");
        verify(permissionRepository, never()).findByModule(any());
    }

    @Test
    void shouldGetPermissionById() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "task:read", "Read Tasks", "task", false);
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        PermissionServiceImpl service = createService();

        PermissionResponse response = service.getPermissionById(permissionId);

        assertThat(response.getId()).isEqualTo(permissionId);
        assertThat(response.getCode()).isEqualTo("task:read");
        assertThat(response.getName()).isEqualTo("Read Tasks");
    }

    @Test
    void shouldThrowWhenPermissionIsNotFoundById() {
        UUID permissionId = UUID.randomUUID();
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        PermissionServiceImpl service = createService();

        assertThatThrownBy(() -> service.getPermissionById(permissionId))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessage("Permission not found: " + permissionId);
    }

    @Test
    void shouldGetPermissionByCode() {
        Permission permission = createPermission(UUID.randomUUID(), "task:read", "Read Tasks", "task", false);
        when(permissionRepository.findByCode("task:read")).thenReturn(Optional.of(permission));

        PermissionServiceImpl service = createService();

        PermissionResponse response = service.getPermissionByCode(" Task:Read ");

        verify(permissionRepository).findByCode("task:read");
        assertThat(response.getCode()).isEqualTo("task:read");
    }

    @Test
    void shouldCreateCustomPermission() {
        CreatePermissionRequest request = createPermissionRequest(
                " Task:Create ",
                " Create Tasks ",
                " Task ",
                " Can create tasks "
        );
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission permission = invocation.getArgument(0);
            permission.setId(UUID.randomUUID());
            return permission;
        });

        PermissionServiceImpl service = createService();

        PermissionResponse response = service.createPermission(request);

        ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(permissionCaptor.capture());
        assertThat(permissionCaptor.getValue().getCode()).isEqualTo("task:create");
        assertThat(permissionCaptor.getValue().getName()).isEqualTo("Create Tasks");
        assertThat(permissionCaptor.getValue().getModule()).isEqualTo("task");
        assertThat(permissionCaptor.getValue().getDescription()).isEqualTo("Can create tasks");
        assertThat(permissionCaptor.getValue().getSystem()).isFalse();
        assertThat(response.getCode()).isEqualTo("task:create");
        assertThat(response.getSystem()).isFalse();
    }

    @Test
    void shouldUpdateCustomPermission() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "task:read", "Read Tasks", "task", false);
        UpdatePermissionRequest request = updatePermissionRequest(
                " Task:Update ",
                " Update Tasks ",
                " Task ",
                " Can update tasks "
        );
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(permissionRepository.save(permission)).thenAnswer(invocation -> invocation.getArgument(0));

        PermissionServiceImpl service = createService();

        PermissionResponse response = service.updatePermission(permissionId, request);

        verify(permissionRepository).save(permission);
        assertThat(permission.getCode()).isEqualTo("task:update");
        assertThat(permission.getName()).isEqualTo("Update Tasks");
        assertThat(permission.getModule()).isEqualTo("task");
        assertThat(permission.getDescription()).isEqualTo("Can update tasks");
        assertThat(response.getCode()).isEqualTo("task:update");
    }

    @Test
    void shouldRejectSystemPermissionUpdate() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "admin:read", "Read Admin", "admin", true);
        UpdatePermissionRequest request = updatePermissionRequest(
                "admin:update",
                "Update Admin",
                "admin",
                "Protected"
        );
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        PermissionServiceImpl service = createService();

        assertThatThrownBy(() -> service.updatePermission(permissionId, request))
                .isInstanceOf(SystemPermissionProtectedException.class)
                .hasMessage("System permission is protected: " + permissionId);
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCustomPermission() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "task:delete", "Delete Tasks", "task", false);
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        PermissionServiceImpl service = createService();

        service.deletePermission(permissionId);

        verify(permissionRepository).delete(permission);
    }

    @Test
    void shouldRejectSystemPermissionDelete() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, "admin:delete", "Delete Admin", "admin", true);
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        PermissionServiceImpl service = createService();

        assertThatThrownBy(() -> service.deletePermission(permissionId))
                .isInstanceOf(SystemPermissionProtectedException.class)
                .hasMessage("System permission is protected: " + permissionId);
        verify(permissionRepository, never()).delete(any());
    }

    @Test
    void shouldGetDistinctPermissionsByRoleIds() {
        UUID firstRoleId = UUID.randomUUID();
        UUID secondRoleId = UUID.randomUUID();
        Permission permission = createPermission(UUID.randomUUID(), "report:read", "Read Reports", "report", false);
        when(permissionRepository.findAllByRoleIds(List.of(firstRoleId, secondRoleId)))
                .thenReturn(List.of(permission));

        PermissionServiceImpl service = createService();

        List<PermissionResponse> responses = service.getPermissionsByRoleIds(
                Arrays.asList(firstRoleId, secondRoleId, firstRoleId, null)
        );

        verify(permissionRepository).findAllByRoleIds(List.of(firstRoleId, secondRoleId));
        assertThat(responses)
                .singleElement()
                .extracting(PermissionResponse::getCode)
                .isEqualTo("report:read");
    }

    @Test
    void shouldReturnEmptyPermissionsWhenRoleIdsAreEmpty() {
        PermissionServiceImpl service = createService();

        List<PermissionResponse> responses = service.getPermissionsByRoleIds(List.of());

        assertThat(responses).isEmpty();
        verify(permissionRepository, never()).findAllByRoleIds(any());
    }

    private PermissionServiceImpl createService() {
        return new PermissionServiceImpl(
                permissionRepository,
                new PermissionBusinessValidator(permissionRepository)
        );
    }

    private static CreatePermissionRequest createPermissionRequest(
            String code,
            String name,
            String module,
            String description
    ) {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode(code);
        request.setName(name);
        request.setModule(module);
        request.setDescription(description);
        return request;
    }

    private static UpdatePermissionRequest updatePermissionRequest(
            String code,
            String name,
            String module,
            String description
    ) {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setCode(code);
        request.setName(name);
        request.setModule(module);
        request.setDescription(description);
        return request;
    }

    private static Permission createPermission(
            UUID permissionId,
            String code,
            String name,
            String module,
            boolean system
    ) {
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setCode(code);
        permission.setName(name);
        permission.setModule(module);
        permission.setSystem(system);
        return permission;
    }
}
