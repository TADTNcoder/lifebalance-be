package com.lifebalance.identity.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.PermissionEvaluationContext;
import com.lifebalance.identity.security.PermissionMatcher;
import com.lifebalance.identity.service.PermissionEvaluationService;
import com.lifebalance.identity.service.RbacAuthorizationService;

import lombok.RequiredArgsConstructor;

@Service("permissionEvaluationService")
@RequiredArgsConstructor
public class PermissionEvaluationServiceImpl implements PermissionEvaluationService {

    private final RbacAuthorizationService rbacAuthorizationService;
    private final UserRepository userRepository;
    private final PermissionMatcher permissionMatcher;

    @Override
    public boolean hasPermission(String permissionKey) {
        return hasPermission(
                SecurityContextHolder.getContext().getAuthentication(),
                permissionKey
        );
    }

    @Override
    public boolean hasPermission(Authentication authentication, String permissionKey) {
        Optional<UserAuthorizationSnapshot> authorization = resolveAuthorization(authentication);
        return authorization
                .map(snapshot -> permissionMatcher.anyMatches(
                        snapshot.permissions(),
                        permissionKey
                ))
                .orElse(false);
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            String targetDomain,
            String action
    ) {
        String permissionKey = permissionMatcher.permissionKey(targetDomain, action);
        if (permissionKey == null) {
            return false;
        }

        return hasPermission(authentication, permissionKey);
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            PermissionEvaluationContext context
    ) {
        if (context == null) {
            return false;
        }

        return hasPermission(
                authentication,
                context.targetDomain(),
                context.action()
        );
    }

    @Override
    public boolean hasAnyPermission(
            Authentication authentication,
            Collection<String> permissionKeys
    ) {
        if (permissionKeys == null || permissionKeys.isEmpty()) {
            return false;
        }

        Optional<UserAuthorizationSnapshot> authorization = resolveAuthorization(authentication);
        return authorization
                .map(snapshot -> permissionKeys.stream()
                        .anyMatch(permissionKey -> permissionMatcher.anyMatches(
                                snapshot.permissions(),
                                permissionKey
                        )))
                .orElse(false);
    }

    @Override
    public boolean hasAllPermissions(
            Authentication authentication,
            Collection<String> permissionKeys
    ) {
        if (permissionKeys == null || permissionKeys.isEmpty()) {
            return false;
        }

        Optional<UserAuthorizationSnapshot> authorization = resolveAuthorization(authentication);
        return authorization
                .map(snapshot -> permissionKeys.stream()
                        .allMatch(permissionKey -> permissionMatcher.anyMatches(
                                snapshot.permissions(),
                                permissionKey
                        )))
                .orElse(false);
    }

    @Override
    public boolean isCurrentUser(Authentication authentication, UUID userId) {
        if (userId == null) {
            return false;
        }

        return resolveInternalUser(authentication)
                .map(user -> userId.equals(user.getId()))
                .orElse(false);
    }

    private Optional<UserAuthorizationSnapshot> resolveAuthorization(Authentication authentication) {
        return resolveInternalUser(authentication)
                .flatMap(user -> {
                    try {
                        return Optional.of(rbacAuthorizationService.getAuthorizationSnapshot(user.getId()));
                    } catch (AppException exception) {
                        return Optional.empty();
                    }
                });
    }

    private Optional<User> resolveInternalUser(Authentication authentication) {
        if (!isUsableAuthentication(authentication)) {
            return Optional.empty();
        }

        String keycloakId = resolveKeycloakId(authentication);
        if (keycloakId == null) {
            return Optional.empty();
        }

        return userRepository.findByKeycloakId(keycloakId)
                .filter(user -> user.getStatus() == AccountStatus.ACTIVE)
                .filter(user -> tokenIssuedAfterValidCutoff(authentication, user));
    }

    private static boolean isUsableAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private static String resolveKeycloakId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
                return null;
            }

            return jwt.getSubject().trim();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            return subject == null || subject.isBlank()
                    ? null
                    : subject.trim();
        }

        String name = authentication.getName();
        return name == null || name.isBlank()
                ? null
                : name.trim();
    }

    private static boolean tokenIssuedAfterValidCutoff(Authentication authentication, User user) {
        if (user.getTokenValidAfter() == null) {
            return true;
        }

        Instant issuedAt = resolveJwt(authentication)
                .map(Jwt::getIssuedAt)
                .orElse(null);
        return issuedAt != null && !issuedAt.isBefore(user.getTokenValidAfter().toInstant());
    }

    private static Optional<Jwt> resolveJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return Optional.ofNullable(jwtAuthenticationToken.getToken());
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.of(jwt);
        }

        return Optional.empty();
    }
}
