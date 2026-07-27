package com.lifebalance.identity.service.impl;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.audit.RoleAuditEventPublisher;
import com.lifebalance.identity.audit.RoleAuditSnapshotMapper;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.exception.PermissionNotFoundException;
import com.lifebalance.identity.exception.RoleNotFoundException;
import com.lifebalance.identity.exception.RoleValidationException;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.repository.RolePermissionRepository;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.RoleBusinessValidator;
import com.lifebalance.identity.service.RoleService;
import com.lifebalance.identity.service.RoleSyncService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import com.lifebalance.identity.model.enums.AuditAction;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleBusinessValidator roleBusinessValidator;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAuthorizationCacheService userAuthorizationCacheService;
    private final RoleAuditSnapshotMapper roleAuditSnapshotMapper;
    private final RoleAuditEventPublisher roleAuditEventPublisher;
    private final RoleSyncService roleSyncService;

    @Transactional
    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        roleBusinessValidator.validateCreate(request);

        Role role = Role.builder()
                .code(normalizeCode(request.getCode()))
                .name(trimToNull(request.getName()))
                .description(trimToNull(request.getDescription()))
                .system(false)
                .build();
        role = roleRepository.save(role);
        List<Permission> permissions = replaceRolePermissions(role, request.getPermissionIds());
        roleSyncService.syncCreatedRole(role);
        String newValue = roleAuditSnapshotMapper.toJson(role, permissions);
        roleAuditEventPublisher.publishRoleAudit(
                AuditAction.CREATE_ROLE,
                role.getId(),
                null,
                newValue,
                "Role created"
        );

        return mapToResponse(role, permissions);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getCode, Comparator.nullsLast(String::compareTo)))
                .toList();
        Map<UUID, List<Permission>> permissionsByRoleId = getPermissionsByRoleId(roles);

        return roles.stream()
                .map(role -> mapToResponse(role, permissionsByRoleId.getOrDefault(role.getId(), List.of())))
                .toList();
    }

    @Override
    public RoleResponse getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        return mapToResponse(role, permissionRepository.findByRoleId(role.getId()));
    }

    @Override
    public RoleResponse getRoleByCode(String code) {
        String normalizedCode = normalizeRequiredCode(code);
        Role role = roleRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new RoleNotFoundException(normalizedCode));

        return mapToResponse(role, permissionRepository.findByRoleId(role.getId()));
    }

    @Transactional
    @Override
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        roleBusinessValidator.validateUpdate(role, request);
        List<Permission> oldPermissions = permissionRepository.findByRoleId(role.getId());
        String oldValue = roleAuditSnapshotMapper.toJson(role, oldPermissions);

        role.setName(trimToNull(request.getName()));
        role.setDescription(trimToNull(request.getDescription()));
        role = roleRepository.save(role);
        List<Permission> permissions = request.getPermissionIds() == null
                ? oldPermissions
                : replaceRolePermissions(role, request.getPermissionIds());
        roleSyncService.syncUpdatedRole(role);
        if (request.getPermissionIds() != null) {
            userAuthorizationCacheService.evictUsersByRoleId(role.getId());
        }
        String newValue = roleAuditSnapshotMapper.toJson(role, permissions);
        roleAuditEventPublisher.publishRoleAudit(
                AuditAction.UPDATE_ROLE,
                role.getId(),
                oldValue,
                newValue,
                "Role updated"
        );

        return mapToResponse(role, permissions);
    }

    @Transactional
    @Override
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        roleBusinessValidator.validateDelete(role);
        List<Permission> permissions = permissionRepository.findByRoleId(role.getId());
        String oldValue = roleAuditSnapshotMapper.toJson(role, permissions);
        roleSyncService.syncDeletedRole(role);
        roleRepository.delete(role);
        userAuthorizationCacheService.evictUsersByRoleId(role.getId());
        roleAuditEventPublisher.publishRoleAudit(
                AuditAction.DELETE_ROLE,
                role.getId(),
                oldValue,
                null,
                "Role deleted"
        );
    }

    @Transactional
    @Override
    public RoleResponse assignPermissionsToRole(UUID roleId, Collection<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        roleBusinessValidator.validateAssignPermissions(role);
        List<Permission> oldPermissions = permissionRepository.findByRoleId(role.getId());
        String oldValue = roleAuditSnapshotMapper.toJson(role, oldPermissions);

        List<Permission> permissions = replaceRolePermissions(role, permissionIds);
        userAuthorizationCacheService.evictUsersByRoleId(role.getId());
        String newValue = roleAuditSnapshotMapper.toJson(role, permissions);
        roleAuditEventPublisher.publishRoleAudit(
                AuditAction.ASSIGN_ROLE_PERMISSIONS,
                role.getId(),
                oldValue,
                newValue,
                "Role permissions changed"
        );

        return mapToResponse(role, permissions);
    }

    private RoleResponse mapToResponse(Role role, List<Permission> permissions) {

        RoleResponse response = new RoleResponse();

        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setSystem(role.getSystem());
        response.setPermissions(mapPermissionResponses(permissions));
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());

        return response;
    }

    private List<PermissionResponse> mapPermissionResponses(List<Permission> permissions) {
        return permissions.stream()
                .sorted(Comparator
                        .comparing(Permission::getModule, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Permission::getCode, Comparator.nullsLast(String::compareTo)))
                .map(this::mapPermissionResponse)
                .toList();
    }

    private PermissionResponse mapPermissionResponse(Permission permission) {
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

    private Map<UUID, List<Permission>> getPermissionsByRoleId(List<Role> roles) {
        List<UUID> roleIds = roles.stream()
                .map(Role::getId)
                .filter(Objects::nonNull)
                .toList();
        if (roleIds.isEmpty()) {
            return Map.of();
        }

        return rolePermissionRepository.findByRoleIds(roleIds).stream()
                .collect(Collectors.groupingBy(
                        rolePermission -> rolePermission.getRole().getId(),
                        Collectors.mapping(RolePermission::getPermission, Collectors.toList())
                ));
    }

    private List<Permission> replaceRolePermissions(Role role, Collection<UUID> permissionIds) {
        List<Permission> permissions = findPermissions(permissionIds);

        rolePermissionRepository.deleteByRoleId(role.getId());
        if (permissions.isEmpty()) {
            return List.of();
        }

        OffsetDateTime grantedAt = OffsetDateTime.now();
        List<RolePermission> rolePermissions = permissions.stream()
                .map(permission -> RolePermission.builder()
                        .id(new RolePermissionId(role.getId(), permission.getId()))
                        .role(role)
                        .permission(permission)
                        .grantedAt(grantedAt)
                        .build())
                .toList();
        rolePermissionRepository.saveAll(rolePermissions);

        return permissions;
    }

    private List<Permission> findPermissions(Collection<UUID> permissionIds) {
        List<UUID> normalizedPermissionIds = normalizePermissionIds(permissionIds);
        if (normalizedPermissionIds.isEmpty()) {
            return List.of();
        }

        List<Permission> permissions = permissionRepository.findAllById(normalizedPermissionIds);
        if (permissions.size() != normalizedPermissionIds.size()) {
            Set<UUID> foundIds = permissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());
            UUID missingId = normalizedPermissionIds.stream()
                    .filter(permissionId -> !foundIds.contains(permissionId))
                    .findFirst()
                    .orElse(normalizedPermissionIds.getFirst());
            throw new PermissionNotFoundException(missingId);
        }

        Map<UUID, Permission> permissionsById = permissions.stream()
                .collect(Collectors.toMap(Permission::getId, Function.identity()));
        return normalizedPermissionIds.stream()
                .map(permissionsById::get)
                .toList();
    }

    private List<UUID> normalizePermissionIds(Collection<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }
        if (permissionIds.stream().anyMatch(Objects::isNull)) {
            throw new RoleValidationException("Permission id is required");
        }

        return permissionIds.stream()
                .distinct()
                .toList();
    }

    private String normalizeRequiredCode(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            throw new RoleValidationException("Role code is required");
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

    private String normalizeCode(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

}
