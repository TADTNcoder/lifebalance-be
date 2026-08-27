package com.lifebalance.identity.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.audit.AuditActor;
import com.lifebalance.identity.audit.AuditRequestMetadata;
import com.lifebalance.identity.audit.CurrentAuditActorResolver;
import com.lifebalance.identity.audit.CurrentAuditRequestMetadataResolver;
import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.exception.RoleNotFoundException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserRoleAssignmentException;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.repository.UserRoleRepository;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import com.lifebalance.identity.service.UserRoleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    private final UserAuthorizationCacheService userAuthorizationCacheService;

    private final AuditLogService auditLogService;

    private final CurrentAuditActorResolver currentAuditActorResolver;

    private final CurrentAuditRequestMetadataResolver currentAuditRequestMetadataResolver;

    @Transactional
    @Override
    public void assignRole(
            UUID userId,
            AssignRoleRequest request,
            String assignedByKeycloakId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException(request.getRoleId()));

        if (userRoleRepository.existsByUserIdAndRoleId(userId, request.getRoleId())) {
            throw UserRoleAssignmentException.alreadyAssigned(userId, request.getRoleId());
        }
        User assigner = userRepository.findByKeycloakId(assignedByKeycloakId)
                .orElseThrow(() -> new UserNotFoundException(assignedByKeycloakId));
        UserRole userRole = new UserRole();

        userRole.setId(new UserRoleId(
                user.getId(),
                role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assigner);
        userRole.setAssignedAt(OffsetDateTime.now());

        userRoleRepository.save(userRole);
        userAuthorizationCacheService.evictUser(userId);
        saveAudit(
                AuditAction.ASSIGN_ROLE,
                user,
                role,
                assigner,
                null,
                role.getCode(),
                "Role assigned to user"
        );
    }

    @Transactional
    @Override
    public void removeRole(
            UUID userId,
            UUID roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw UserRoleAssignmentException.notAssigned(userId, roleId);
        }

        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        userAuthorizationCacheService.evictUser(userId);
        saveAudit(
                AuditAction.REVOKE_ROLE,
                user,
                role,
                null,
                role.getCode(),
                null,
                "Role revoked from user"
        );
    }

    @Override
    @Transactional
    public List<RoleResponse> getRoles(UUID userId) {

        return userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRole)
                .map(this::mapToResponse)
                .toList();
    }

    private RoleResponse mapToResponse(Role role) {

        RoleResponse response = new RoleResponse();

        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setSystem(role.getSystem());
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());

        return response;
    }

    private void saveAudit(
            AuditAction action,
            User user,
            Role role,
            User fallbackActor,
            String oldValue,
            String newValue,
            String details
    ) {
        AuditActor actor = currentAuditActorResolver.resolve();
        AuditRequestMetadata metadata = currentAuditRequestMetadataResolver.resolve();
        UUID actorId = actor.id() != null
                ? actor.id()
                : fallbackActor == null ? null : fallbackActor.getId();
        String actorKeycloakId = firstNonBlank(
                actor.keycloakId(),
                fallbackActor == null ? null : fallbackActor.getKeycloakId()
        );
        String actorUsername = firstNonBlank(
                actor.username(),
                fallbackActor == null ? null : fallbackActor.getUsername()
        );

        auditLogService.saveAudit(new AuditLogCommand(
                AuditEntityName.USER_ROLE,
                entityId(user.getId(), role.getId()),
                actorId,
                actorKeycloakId,
                actorUsername,
                user.getId(),
                user.getKeycloakId(),
                action,
                AuditStatus.SUCCESS,
                metadata.ipAddress(),
                metadata.userAgent(),
                oldValue,
                newValue,
                details
        ));
    }

    private String entityId(UUID userId, UUID roleId) {
        return userId + ":" + roleId;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        return second == null || second.isBlank() ? null : second;
    }
}
