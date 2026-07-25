package com.lifebalance.identity.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.exception.RoleCodeAlreadyExistsException;
import com.lifebalance.identity.exception.RoleNameAlreadyExistsException;
import com.lifebalance.identity.exception.RoleValidationException;
import com.lifebalance.identity.exception.SystemRoleCreationNotAllowedException;
import com.lifebalance.identity.exception.SystemRoleProtectedException;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.repository.RoleRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleBusinessValidatorTest {

    @Mock
    private RoleRepository roleRepository;

    @Test
    void shouldValidateRoleCreation() {
        CreateRoleRequest request = createRoleRequest(" manager ", " Manager ");
        RoleBusinessValidator validator = createValidator();

        validator.validateCreate(request);

        verify(roleRepository).existsByCode("manager");
        verify(roleRepository).existsByName("Manager");
    }

    @Test
    void shouldRejectDuplicateRoleCodeOnCreate() {
        CreateRoleRequest request = createRoleRequest("admin", "Administrator");
        when(roleRepository.existsByCode("admin")).thenReturn(true);

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(RoleCodeAlreadyExistsException.class)
                .hasMessage("Role code already exists: admin");
        verify(roleRepository, never()).existsByName("Administrator");
    }

    @Test
    void shouldRejectDuplicateRoleNameOnCreate() {
        CreateRoleRequest request = createRoleRequest("admin", "Administrator");
        when(roleRepository.existsByName("Administrator")).thenReturn(true);

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(RoleNameAlreadyExistsException.class)
                .hasMessage("Role name already exists: Administrator");
    }

    @Test
    void shouldRejectSystemRoleCreationFromPublicRequest() {
        CreateRoleRequest request = createRoleRequest("admin", "Administrator");
        request.setSystem(true);

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SystemRoleCreationNotAllowedException.class)
                .hasMessage("System role creation is not allowed");
        verify(roleRepository, never()).existsByCode("admin");
    }

    @Test
    void shouldRejectBlankCodeOnCreate() {
        CreateRoleRequest request = createRoleRequest(" ", "Administrator");

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(RoleValidationException.class)
                .hasMessage("Role code is required");
        verify(roleRepository, never()).existsByCode("admin");
    }

    @Test
    void shouldValidateRoleUpdate() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        UpdateRoleRequest request = updateRoleRequest(" Manager ");

        RoleBusinessValidator validator = createValidator();

        validator.validateUpdate(role, request);

        verify(roleRepository).existsByNameAndIdNot("Manager", roleId);
    }

    @Test
    void shouldRejectDuplicateRoleNameOnUpdate() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        UpdateRoleRequest request = updateRoleRequest("Manager");
        when(roleRepository.existsByNameAndIdNot("Manager", roleId)).thenReturn(true);

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateUpdate(role, request))
                .isInstanceOf(RoleNameAlreadyExistsException.class)
                .hasMessage("Role name already exists: Manager");
    }

    @Test
    void shouldRejectSystemRoleUpdate() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);
        UpdateRoleRequest request = updateRoleRequest("Administrator");

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateUpdate(role, request))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
        verify(roleRepository, never()).existsByNameAndIdNot("Administrator", roleId);
    }

    @Test
    void shouldRejectSystemFlagChangeOnUpdate() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        UpdateRoleRequest request = updateRoleRequest("Custom Role");
        request.setSystem(true);

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateUpdate(role, request))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
    }

    @Test
    void shouldRejectSystemRoleDelete() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);

        RoleBusinessValidator validator = createValidator();

        assertThatThrownBy(() -> validator.validateDelete(role))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
    }

    private RoleBusinessValidator createValidator() {
        return new RoleBusinessValidator(roleRepository);
    }

    private static CreateRoleRequest createRoleRequest(String code, String name) {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode(code);
        request.setName(name);
        return request;
    }

    private static UpdateRoleRequest updateRoleRequest(String name) {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName(name);
        return request;
    }

    private static Role createRole(UUID roleId, boolean system) {
        Role role = new Role();
        role.setId(roleId);
        role.setCode("role");
        role.setName("Role");
        role.setSystem(system);
        return role;
    }
}
