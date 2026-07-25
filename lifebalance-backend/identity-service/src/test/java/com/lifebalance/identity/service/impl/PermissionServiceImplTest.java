package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.repository.PermissionRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void shouldCreatePermissionSuccessfully() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode("task:read");
        request.setName("Read Task");
        request.setModule("Task");
        request.setDescription("Allows reading tasks");

        when(permissionRepository.existsByCode("task:read")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> {
            Permission p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PermissionResponse response = permissionService.create(request);

        assertThat(response.getCode()).isEqualTo("task:read");
        assertThat(response.getName()).isEqualTo("Read Task");
        assertThat(response.getModule()).isEqualTo("Task");
        assertThat(response.getDescription()).isEqualTo("Allows reading tasks");
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void shouldThrowWhenPermissionCodeAlreadyExists() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode("task:read");

        when(permissionRepository.existsByCode("task:read")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Permission code already exists");
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void shouldGetAllPermissions() {
        Permission p = new Permission();
        p.setId(UUID.randomUUID());
        p.setCode("task:read");
        when(permissionRepository.findAll()).thenReturn(List.of(p));

        List<PermissionResponse> list = permissionService.getAll();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCode()).isEqualTo("task:read");
    }

    @Test
    void shouldGetPermissionById() {
        UUID id = UUID.randomUUID();
        Permission p = new Permission();
        p.setId(id);
        p.setCode("task:read");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(p));

        PermissionResponse response = permissionService.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getCode()).isEqualTo("task:read");
    }

    @Test
    void shouldThrowWhenPermissionNotFoundById() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Permission not found");
    }

    @Test
    void shouldUpdatePermission() {
        UUID id = UUID.randomUUID();
        Permission p = new Permission();
        p.setId(id);
        p.setCode("task:read");

        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setName("Updated Name");
        request.setModule("newmodule"); // Đã khớp chữ thường theo logic service
        request.setDescription("New Desc");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(p));
        when(permissionRepository.save(p)).thenReturn(p);

        PermissionResponse response = permissionService.update(id, request);

        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getModule()).isEqualTo("newmodule");
        assertThat(response.getDescription()).isEqualTo("New Desc");
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentPermission() {
        UUID id = UUID.randomUUID();
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.update(id, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Permission not found");
    }

    @Test
    void shouldDeletePermission() {
        UUID id = UUID.randomUUID();
        Permission p = new Permission();
        p.setId(id);

        when(permissionRepository.findById(id)).thenReturn(Optional.of(p));

        permissionService.delete(id);

        verify(permissionRepository).delete(p);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentPermission() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.delete(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Permission not found");
    }
}