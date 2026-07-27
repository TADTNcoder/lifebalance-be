package com.lifebalance.identity.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionBusinessValidator {

    private final PermissionRepository permissionRepository;

    public void validateCreate(CreatePermissionRequest request) {
        if (request == null) {
            throw new PermissionValidationException("Permission request is required");
        }

        String code = normalizeRequired(request.getCode(), "Permission code is required");
        normalizeRequired(request.getName(), "Permission name is required");
        normalizeRequired(request.getModule(), "Permission module is required");

        if (Boolean.TRUE.equals(request.getSystem())) {
            throw new SystemPermissionCreationNotAllowedException();
        }
        if (permissionRepository.existsByCode(code)) {
            throw new PermissionAlreadyExistsException(code);
        }
    }

    public void validateUpdate(Permission permission, UpdatePermissionRequest request) {
        requirePermission(permission);
        if (request == null) {
            throw new PermissionValidationException("Permission request is required");
        }
        validateSystemPermissionIsNotModified(permission);

        String code = normalizeRequired(request.getCode(), "Permission code is required");
        normalizeRequired(request.getName(), "Permission name is required");
        normalizeRequired(request.getModule(), "Permission module is required");

        UUID permissionId = permission.getId();
        if (request.getSystem() != null
                && !request.getSystem().equals(permission.getSystem())) {
            throw new SystemPermissionProtectedException(permissionId);
        }
        if (permissionRepository.existsByCodeAndIdNot(code, permissionId)) {
            throw new PermissionAlreadyExistsException(code);
        }
    }

    public void validateDelete(Permission permission) {
        requirePermission(permission);
        validateSystemPermissionIsNotModified(permission);
        if (permissionRepository.existsAssignedToActiveRole(permission.getId())) {
            throw new PermissionAssignedToRoleException(permission.getId());
        }
    }

    private void requirePermission(Permission permission) {
        if (permission == null || permission.getId() == null) {
            throw new PermissionValidationException("Permission is required");
        }
    }

    private void validateSystemPermissionIsNotModified(Permission permission) {
        if (Boolean.TRUE.equals(permission.getSystem())) {
            throw new SystemPermissionProtectedException(permission.getId());
        }
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new PermissionValidationException(message);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
