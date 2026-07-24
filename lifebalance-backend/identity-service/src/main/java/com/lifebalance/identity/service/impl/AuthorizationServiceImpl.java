package com.lifebalance.identity.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.CheckPermissionResponse;
import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuthorizationService;
import com.lifebalance.identity.service.RbacAuthorizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final RbacAuthorizationService rbacAuthorizationService;

    @Override
    public CheckPermissionResponse checkPermission(
            User user,
            CurrentUser currentUser,
            String permissionCode
    ) {
        List<String> tokenRoles = normalizeList(currentUser.getRoles());
        UserAuthorizationSnapshot authorization =
                rbacAuthorizationService.getAuthorizationSnapshot(user.getId());
        List<String> roles = List.copyOf(authorization.roles());
        List<String> permissions = List.copyOf(authorization.permissions());
        String requestedPermission = normalize(permissionCode);
        Boolean hasPermission = requestedPermission == null
                ? null
                : rbacAuthorizationService.hasPermission(authorization, requestedPermission);

        return new CheckPermissionResponse(
                true,
                user.getId(),
                user.getKeycloakId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                tokenRoles,
                roles,
                permissions,
                requestedPermission,
                hasPermission
        );
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .map(AuthorizationServiceImpl::normalize)
                .filter(value -> value != null)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty()
                ? null
                : normalized.toLowerCase(Locale.ROOT);
    }
}
