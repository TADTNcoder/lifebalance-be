package com.lifebalance.identity.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.CheckPermissionResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuthorizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserRepository userRepository;

    @Override
    public CheckPermissionResponse checkPermission(
            User user,
            CurrentUser currentUser,
            String permissionCode
    ) {
        List<String> tokenRoles = normalizeList(currentUser.getRoles());
        List<String> roles = normalizeList(
                userRepository.findRoleCodesByUserId(user.getId())
        );
        List<String> permissions = normalizeList(
                userRepository.findPermissionCodesByUserId(user.getId())
        );
        String requestedPermission = normalize(permissionCode);
        Boolean hasPermission = requestedPermission == null
                ? null
                : containsIgnoreCase(permissions, requestedPermission);

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
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean containsIgnoreCase(
            List<String> values,
            String target
    ) {
        String normalizedTarget = target.toLowerCase(Locale.ROOT);

        return values.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(normalizedTarget));
    }
}
