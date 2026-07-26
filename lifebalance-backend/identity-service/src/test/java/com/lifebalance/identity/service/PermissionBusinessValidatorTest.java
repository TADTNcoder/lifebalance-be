package com.lifebalance.identity.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.exception.PermissionAlreadyExistsException;
import com.lifebalance.identity.exception.PermissionAssignedToRoleException;
import com.lifebalance.identity.exception.PermissionValidationException;
import com.lifebalance.identity.exception.SystemPermissionCreationNotAllowedException;
import com.lifebalance.identity.exception.SystemPermissionProtectedException;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.repository.PermissionRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionBusinessValidatorTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Test
    void shouldValidatePermissionCreation() {
        CreatePermissionRequest request = createPermissionRequest(
                " Task:Create ",
                " Create Tasks ",
                " Task "
        );
        PermissionBusinessValidator validator = createValidator();

        validator.validateCreate(request);

        verify(permissionRepository).existsByCode("Task:Create");
    }

    @Test
    void shouldRejectDuplicatePermissionCodeOnCreate() {
        CreatePermissionRequest request = createPermissionRequest(
                "task:create",
                "Create Tasks",
                "task"
        );
        when(permissionRepository.existsByCode("task:create")).thenReturn(true);

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(PermissionAlreadyExistsException.class)
                .hasMessage("Permission code already exists: task:create");
    }

    @Test
    void shouldRejectSystemPermissionCreationFromPublicRequest() {
        CreatePermissionRequest request = createPermissionRequest(
                "admin:read",
                "Read Admin",
                "admin"
        );
        request.setSystem(true);

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SystemPermissionCreationNotAllowedException.class)
                .hasMessage("System permission creation is not allowed");
        verify(permissionRepository, never()).existsByCode("admin:read");
    }

    @Test
    void shouldRejectBlankModuleOnCreate() {
        CreatePermissionRequest request = createPermissionRequest(
                "admin:read",
                "Read Admin",
                " "
        );

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(PermissionValidationException.class)
                .hasMessage("Permission module is required");
        verify(permissionRepository, never()).existsByCode("admin:read");
    }

    @Test
    void shouldValidatePermissionUpdate() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, false);
        UpdatePermissionRequest request = updatePermissionRequest(
                " Task:Update ",
                " Update Tasks ",
                " Task "
        );

        PermissionBusinessValidator validator = createValidator();

        validator.validateUpdate(permission, request);

        verify(permissionRepository).existsByCodeAndIdNot("Task:Update", permissionId);
    }

    @Test
    void shouldRejectDuplicatePermissionCodeOnUpdate() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, false);
        UpdatePermissionRequest request = updatePermissionRequest(
                "task:update",
                "Update Tasks",
                "task"
        );
        when(permissionRepository.existsByCodeAndIdNot("task:update", permissionId)).thenReturn(true);

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateUpdate(permission, request))
                .isInstanceOf(PermissionAlreadyExistsException.class)
                .hasMessage("Permission code already exists: task:update");
    }

    @Test
    void shouldRejectSystemPermissionUpdate() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, true);
        UpdatePermissionRequest request = updatePermissionRequest(
                "admin:update",
                "Update Admin",
                "admin"
        );

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateUpdate(permission, request))
                .isInstanceOf(SystemPermissionProtectedException.class)
                .hasMessage("System permission is protected: " + permissionId);
        verify(permissionRepository, never()).existsByCodeAndIdNot("admin:update", permissionId);
    }

    @Test
    void shouldRejectSystemFlagChangeOnUpdate() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, false);
        UpdatePermissionRequest request = updatePermissionRequest(
                "task:update",
                "Update Tasks",
                "task"
        );
        request.setSystem(true);

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateUpdate(permission, request))
                .isInstanceOf(SystemPermissionProtectedException.class)
                .hasMessage("System permission is protected: " + permissionId);
    }

    @Test
    void shouldRejectAssignedPermissionDelete() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, false);
        when(permissionRepository.existsAssignedToActiveRole(permissionId)).thenReturn(true);

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateDelete(permission))
                .isInstanceOf(PermissionAssignedToRoleException.class)
                .hasMessage("Permission is assigned to at least one role: " + permissionId);
    }

    @Test
    void shouldRejectSystemPermissionDelete() {
        UUID permissionId = UUID.randomUUID();
        Permission permission = createPermission(permissionId, true);

        PermissionBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateDelete(permission))
                .isInstanceOf(SystemPermissionProtectedException.class)
                .hasMessage("System permission is protected: " + permissionId);
        verify(permissionRepository, never()).existsAssignedToActiveRole(permissionId);
    }

    private PermissionBusinessValidator createValidator() {
        return new PermissionBusinessValidator(permissionRepository);
    }

    private static CreatePermissionRequest createPermissionRequest(
            String code,
            String name,
            String module
    ) {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode(code);
        request.setName(name);
        request.setModule(module);
        return request;
    }

    private static UpdatePermissionRequest updatePermissionRequest(
            String code,
            String name,
            String module
    ) {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setCode(code);
        request.setName(name);
        request.setModule(module);
        return request;
    }

    private static Permission createPermission(UUID permissionId, boolean system) {
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setCode("task:read");
        permission.setName("Read Tasks");
        permission.setModule("task");
        permission.setSystem(system);
        return permission;
    }
}
