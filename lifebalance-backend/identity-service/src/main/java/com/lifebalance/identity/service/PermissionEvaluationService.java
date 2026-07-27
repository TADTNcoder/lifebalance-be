package com.lifebalance.identity.service;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.lifebalance.identity.security.PermissionEvaluationContext;

public interface PermissionEvaluationService {

    boolean hasPermission(String permissionKey);

    boolean hasPermission(Authentication authentication, String permissionKey);

    boolean hasPermission(Authentication authentication, String targetDomain, String action);

    boolean hasPermission(Authentication authentication, PermissionEvaluationContext context);

    boolean hasAnyPermission(Authentication authentication, Collection<String> permissionKeys);

    boolean hasAllPermissions(Authentication authentication, Collection<String> permissionKeys);

    boolean isCurrentUser(Authentication authentication, UUID userId);
}
