package com.lifebalance.identity.service.impl;

import static com.lifebalance.identity.config.RbacCacheConfig.USER_AUTHORIZATION_SNAPSHOTS_CACHE;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.service.RbacAuthorizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RbacAuthorizationServiceImpl implements RbacAuthorizationService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Override
    public boolean hasPermission(UUID userId, String permissionCode) {
        return hasPermission(getAuthorizationSnapshot(userId), permissionCode);
    }

    @Override
    public boolean hasPermission(UserAuthorizationSnapshot authorization, String permissionCode) {
        String normalizedPermission = normalize(permissionCode);
        if (authorization == null || normalizedPermission == null) {
            return false;
        }

        return authorization.permissions().contains(normalizedPermission);
    }

    @Override
    public boolean hasRole(UUID userId, String roleCode) {
        return hasRole(getAuthorizationSnapshot(userId), roleCode);
    }

    @Override
    public boolean hasRole(UserAuthorizationSnapshot authorization, String roleCode) {
        String normalizedRole = normalize(roleCode);
        if (authorization == null || normalizedRole == null) {
            return false;
        }

        return authorization.roles().contains(normalizedRole);
    }

    @Override
    public Set<String> getUserPermissions(UUID userId) {
        return getAuthorizationSnapshot(userId).permissions();
    }

    @Override
    public Set<String> getUserRoles(UUID userId) {
        return getAuthorizationSnapshot(userId).roles();
    }

    @Override
    public UserAuthorizationSnapshot getAuthorizationSnapshot(UUID userId) {
        validateUserId(userId);

        Cache cache = cacheManager.getCache(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
        if (cache == null) {
            return loadAuthorizationSnapshot(userId);
        }

        UserAuthorizationSnapshot cachedSnapshot = cache.get(userId, UserAuthorizationSnapshot.class);
        if (cachedSnapshot != null) {
            return cachedSnapshot;
        }

        UserAuthorizationSnapshot loadedSnapshot = loadAuthorizationSnapshot(userId);
        cache.put(userId, loadedSnapshot);

        return loadedSnapshot;
    }

    @Override
    public void requirePermission(UUID userId, String permissionCode) {
        if (!hasPermission(userId, permissionCode)) {
            throw new AccessDeniedException("Missing required permission: " + permissionCode);
        }
    }

    @Override
    public void requireRole(UUID userId, String roleCode) {
        if (!hasRole(userId, roleCode)) {
            throw new AccessDeniedException("Missing required role: " + roleCode);
        }
    }

    @Override
    public void evictUserAuthorization(UUID userId) {
        validateUserId(userId);

        Cache cache = cacheManager.getCache(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
        if (cache != null) {
            cache.evict(userId);
        }
    }

    private UserAuthorizationSnapshot loadAuthorizationSnapshot(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UserInactiveException(user.getStatus());
        }

        return new UserAuthorizationSnapshot(
                userId,
                normalizeSet(userRepository.findRoleCodesByUserId(userId)),
                normalizeSet(userRepository.findPermissionCodesByUserId(userId))
        );
    }

    private static Set<String> normalizeSet(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return values.stream()
                .map(RbacAuthorizationServiceImpl::normalize)
                .filter(value -> value != null)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new UserValidationException("User id is required");
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
