package com.lifebalance.identity.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.repository.RolePermissionRepository;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.RolePermissionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    @Override
    public void assignPermission(
            UUID roleId,
            AssignPermissionRequest request) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if (rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(
                roleId,
                permission.getId())) {

            throw new RuntimeException("Permission already assigned");
        }

        RolePermission rolePermission = RolePermission.builder()
                .id(new RolePermissionId(
                        roleId,
                        permission.getId()))
                .role(role)
                .permission(permission)
                .grantedAt(OffsetDateTime.now())
                .build();

        rolePermissionRepository.save(rolePermission);
    }

    @Transactional
    @Override
    public void removePermission(
            UUID roleId,
            UUID permissionId) {

        if (!rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(
                roleId,
                permissionId)) {

            throw new RuntimeException("Permission not assigned");
        }

        rolePermissionRepository.deleteByIdRoleIdAndIdPermissionId(
                roleId,
                permissionId);
    }

    @Override
    public List<PermissionResponse> getPermissions(UUID roleId) {

        return rolePermissionRepository.findByIdRoleId(roleId)
                .stream()
                .map(RolePermission::getPermission)
                .map(this::mapToResponse)
                .toList();
    }

    private PermissionResponse mapToResponse(Permission permission) {

        PermissionResponse response = new PermissionResponse();

        response.setId(permission.getId());
        response.setCode(permission.getCode());
        response.setName(permission.getName());
        response.setModule(permission.getModule());
        response.setDescription(permission.getDescription());
        response.setCreatedAt(permission.getCreatedAt());
        response.setUpdatedAt(permission.getUpdatedAt());

        return response;
    }
}
