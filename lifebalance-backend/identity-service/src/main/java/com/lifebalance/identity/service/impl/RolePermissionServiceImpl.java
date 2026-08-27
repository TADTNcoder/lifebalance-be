package com.lifebalance.identity.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.lifebalance.identity.audit.AuditActor;
import com.lifebalance.identity.audit.AuditRequestMetadata;
import com.lifebalance.identity.audit.CurrentAuditActorResolver;
import com.lifebalance.identity.audit.CurrentAuditRequestMetadataResolver;
import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.exception.PermissionNotFoundException;
import com.lifebalance.identity.exception.RoleNotFoundException;
import com.lifebalance.identity.exception.RolePermissionAssignmentException;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.repository.RolePermissionRepository;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.RolePermissionService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final RolePermissionRepository rolePermissionRepository;

    private final UserAuthorizationCacheService userAuthorizationCacheService;

    private final AuditLogService auditLogService;

    private final CurrentAuditActorResolver currentAuditActorResolver;

    private final CurrentAuditRequestMetadataResolver currentAuditRequestMetadataResolver;

    @Transactional
    @Override
    public void assignPermission(
            UUID roleId,
            AssignPermissionRequest request) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new PermissionNotFoundException(request.getPermissionId()));

        if (rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(
                roleId,
                permission.getId())) {

            throw RolePermissionAssignmentException.alreadyAssigned(roleId, permission.getId());
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
        userAuthorizationCacheService.evictUsersByRoleId(roleId);
        saveAudit(
                AuditAction.ASSIGN_PERMISSION,
                role,
                permission,
                null,
                permission.getCode(),
                "Permission assigned to role"
        );
    }

    @Transactional
    @Override
    public void removePermission(
            UUID roleId,
            UUID permissionId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));

        if (!rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(
                roleId,
                permissionId)) {

            throw RolePermissionAssignmentException.notAssigned(roleId, permissionId);
        }

        rolePermissionRepository.deleteByIdRoleIdAndIdPermissionId(
                roleId,
                permissionId);
        userAuthorizationCacheService.evictUsersByRoleId(roleId);
        saveAudit(
                AuditAction.REVOKE_PERMISSION,
                role,
                permission,
                permission.getCode(),
                null,
                "Permission revoked from role"
        );
    }

    @Override
    @Transactional
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

    private void saveAudit(
            AuditAction action,
            Role role,
            Permission permission,
            String oldValue,
            String newValue,
            String details
    ) {
        AuditActor actor = currentAuditActorResolver.resolve();
        AuditRequestMetadata metadata = currentAuditRequestMetadataResolver.resolve();

        auditLogService.saveAudit(new AuditLogCommand(
                AuditEntityName.ROLE_PERMISSION,
                entityId(role.getId(), permission.getId()),
                actor.id(),
                actor.keycloakId(),
                actor.username(),
                null,
                null,
                action,
                AuditStatus.SUCCESS,
                metadata.ipAddress(),
                metadata.userAgent(),
                oldValue,
                newValue,
                details
        ));
    }

    private String entityId(UUID roleId, UUID permissionId) {
        return roleId + ":" + permissionId;
    }
}
