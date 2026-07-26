package com.lifebalance.identity.service;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.exception.RoleAssignedToUserException;
import com.lifebalance.identity.exception.RoleCodeAlreadyExistsException;
import com.lifebalance.identity.exception.RoleNameAlreadyExistsException;
import com.lifebalance.identity.exception.RoleValidationException;
import com.lifebalance.identity.exception.SystemRoleCreationNotAllowedException;
import com.lifebalance.identity.exception.SystemRoleProtectedException;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.repository.RoleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleBusinessValidator {

    private final RoleRepository roleRepository;

    public void validateCreate(CreateRoleRequest request) {
        if (request == null) {
            throw new RoleValidationException("Role request is required");
        }

        String code = normalizeRequired(request.getCode(), "Role code is required");
        String name = normalizeRequired(request.getName(), "Role name is required");

        if (Boolean.TRUE.equals(request.getSystem())) {
            throw new SystemRoleCreationNotAllowedException();
        }
        if (roleRepository.existsByCode(code)) {
            throw new RoleCodeAlreadyExistsException(code);
        }
        if (roleRepository.existsByName(name)) {
            throw new RoleNameAlreadyExistsException(name);
        }
    }

    public void validateUpdate(Role role, UpdateRoleRequest request) {
        requireRole(role);
        if (request == null) {
            throw new RoleValidationException("Role request is required");
        }
        validateSystemRoleIsNotModified(role);

        String name = normalizeRequired(request.getName(), "Role name is required");
        UUID roleId = role.getId();

        if (request.getSystem() != null
                && !request.getSystem().equals(role.getSystem())) {
            throw new SystemRoleProtectedException(roleId);
        }
        if (roleRepository.existsByNameAndIdNot(name, roleId)) {
            throw new RoleNameAlreadyExistsException(name);
        }
    }

    public void validateDelete(Role role) {
        requireRole(role);
        validateSystemRoleIsNotModified(role);
        if (roleRepository.existsAssignedToActiveUser(role.getId())) {
            throw new RoleAssignedToUserException(role.getId());
        }
    }

    public void validateAssignPermissions(Role role) {
        requireRole(role);
        validateSystemRoleIsNotModified(role);
    }

    private void requireRole(Role role) {
        if (role == null || role.getId() == null) {
            throw new RoleValidationException("Role is required");
        }
    }

    private void validateSystemRoleIsNotModified(Role role) {
        if (Boolean.TRUE.equals(role.getSystem())) {
            throw new SystemRoleProtectedException(role.getId());
        }
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new RoleValidationException(message);
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
