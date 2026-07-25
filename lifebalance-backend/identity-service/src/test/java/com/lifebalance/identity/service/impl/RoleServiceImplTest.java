package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.exception.RoleNotFoundException;
import com.lifebalance.identity.exception.SystemRoleProtectedException;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.RoleBusinessValidator;
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

    @Test
    void shouldCreateCustomRole() {
        CreateRoleRequest request = createRoleRequest(" Manager ", " Manager ", " Operational managers ");
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(UUID.randomUUID());
            return role;
        });

        RoleServiceImpl service = createService();

        RoleResponse response = service.create(request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getCode()).isEqualTo("manager");
        assertThat(roleCaptor.getValue().getName()).isEqualTo("Manager");
        assertThat(roleCaptor.getValue().getDescription()).isEqualTo("Operational managers");
        assertThat(roleCaptor.getValue().getSystem()).isFalse();
        assertThat(response.getCode()).isEqualTo("manager");
        assertThat(response.getName()).isEqualTo("Manager");
        assertThat(response.getSystem()).isFalse();
    }

    @Test
    void shouldThrowWhenRoleIsNotFoundById() {
        UUID roleId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.getById(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found: " + roleId);
    }

    @Test
    void shouldUpdateCustomRole() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        UpdateRoleRequest request = updateRoleRequest(" Updated Manager ", " Updated description ");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenAnswer(invocation -> invocation.getArgument(0));

        RoleServiceImpl service = createService();

        RoleResponse response = service.update(roleId, request);

        verify(roleRepository).save(role);
        assertThat(role.getName()).isEqualTo("Updated Manager");
        assertThat(role.getDescription()).isEqualTo("Updated description");
        assertThat(role.getSystem()).isFalse();
        assertThat(response.getName()).isEqualTo("Updated Manager");
    }

    @Test
    void shouldRejectSystemRoleUpdate() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);
        UpdateRoleRequest request = updateRoleRequest("Administrator", "Protected");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.update(roleId, request))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCustomRole() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        service.delete(roleId);

        verify(roleRepository).delete(role);
    }

    @Test
    void shouldRejectSystemRoleDelete() {
        UUID roleId = UUID.randomUUID();
        Role role = createRole(roleId, true);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        RoleServiceImpl service = createService();

        assertThatThrownBy(() -> service.delete(roleId))
                .isInstanceOf(SystemRoleProtectedException.class)
                .hasMessage("System role is protected: " + roleId);
        verify(roleRepository, never()).delete(any());
    }

    private RoleServiceImpl createService() {
        return new RoleServiceImpl(roleRepository, new RoleBusinessValidator(roleRepository));
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
}
