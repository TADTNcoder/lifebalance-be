package com.lifebalance.identity.service.impl;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.audit.PermissionAuditEventPublisher;
import com.lifebalance.identity.audit.PermissionAuditSnapshotMapper;
import com.lifebalance.identity.exception.PermissionNotFoundException;
import com.lifebalance.identity.exception.PermissionValidationException;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.service.PermissionBusinessValidator;
import com.lifebalance.identity.service.PermissionService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionBusinessValidator permissionBusinessValidator;
    private final UserAuthorizationCacheService userAuthorizationCacheService;
    private final PermissionAuditSnapshotMapper permissionAuditSnapshotMapper;
    private final PermissionAuditEventPublisher permissionAuditEventPublisher;

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PermissionResponse> getPermissionsByModule(String module) {
        String normalizedModule = normalizeRequired(module, "Permission module is required");

        return permissionRepository.findByModule(normalizedModule).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PermissionResponse getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));

        return mapToResponse(permission);
    }

    @Override
    public PermissionResponse getPermissionByCode(String code) {
        String normalizedCode = normalizeRequired(code, "Permission code is required");
        Permission permission = permissionRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new PermissionNotFoundException(normalizedCode));

        return mapToResponse(permission);
    }

    @Transactional
    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        permissionBusinessValidator.validateCreate(request);

        Permission permission = Permission.builder()
                .code(normalizeKey(request.getCode()))
                .name(trimToNull(request.getName()))
                .module(normalizeKey(request.getModule()))
                .description(trimToNull(request.getDescription()))
                .system(false)
                .build();
        permission = permissionRepository.save(permission);
        String newValue = permissionAuditSnapshotMapper.toJson(permission);
        permissionAuditEventPublisher.publishPermissionAudit(
                AuditAction.CREATE_PERMISSION,
                permission.getId(),
                null,
                newValue,
                "Permission created"
        );

        return mapToResponse(permission);
    }

    @Transactional
    @Override
    public PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        permissionBusinessValidator.validateUpdate(permission, request);
        String oldValue = permissionAuditSnapshotMapper.toJson(permission);

        permission.setCode(normalizeKey(request.getCode()));
        permission.setName(trimToNull(request.getName()));
        permission.setModule(normalizeKey(request.getModule()));
        permission.setDescription(trimToNull(request.getDescription()));
        permission = permissionRepository.save(permission);
        userAuthorizationCacheService.evictUsersByPermissionId(permission.getId());
        String newValue = permissionAuditSnapshotMapper.toJson(permission);
        permissionAuditEventPublisher.publishPermissionAudit(
                AuditAction.UPDATE_PERMISSION,
                permission.getId(),
                oldValue,
                newValue,
                "Permission updated"
        );

        return mapToResponse(permission);
    }

    @Transactional
    @Override
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        permissionBusinessValidator.validateDelete(permission);
        String oldValue = permissionAuditSnapshotMapper.toJson(permission);
        permissionRepository.delete(permission);
        userAuthorizationCacheService.evictUsersByPermissionId(permission.getId());
        permissionAuditEventPublisher.publishPermissionAudit(
                AuditAction.DELETE_PERMISSION,
                permission.getId(),
                oldValue,
                null,
                "Permission deleted"
        );
    }

    @Override
    public List<PermissionResponse> getPermissionsByRoleIds(Collection<UUID> roleIds) {
        List<UUID> normalizedRoleIds = normalizeRoleIds(roleIds);
        if (normalizedRoleIds.isEmpty()) {
            return List.of();
        }

        return permissionRepository.findAllByRoleIds(normalizedRoleIds).stream()
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
        response.setSystem(permission.getSystem());
        response.setCreatedAt(permission.getCreatedAt());
        response.setUpdatedAt(permission.getUpdatedAt());

        return response;
    }

    private List<UUID> normalizeRoleIds(Collection<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        return roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeKey(value);
        if (normalized == null) {
            throw new PermissionValidationException(message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeKey(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
