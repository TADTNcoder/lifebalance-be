package com.lifebalance.identity.service;

import java.util.Set;
import java.util.UUID;

import com.lifebalance.identity.dto.UserAuthorizationSnapshot;

public interface RbacAuthorizationService {

    boolean hasPermission(UUID userId, String permissionCode);

    boolean hasPermission(UserAuthorizationSnapshot authorization, String permissionCode);

    boolean hasRole(UUID userId, String roleCode);

    boolean hasRole(UserAuthorizationSnapshot authorization, String roleCode);

    Set<String> getUserPermissions(UUID userId);

    Set<String> getUserRoles(UUID userId);

    UserAuthorizationSnapshot getAuthorizationSnapshot(UUID userId);

    void requirePermission(UUID userId, String permissionCode);

    void requireRole(UUID userId, String roleCode);

    void evictUserAuthorization(UUID userId);
}
