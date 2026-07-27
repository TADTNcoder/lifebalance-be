package com.lifebalance.identity.security;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.PermissionEvaluationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private static final String SEPARATOR = ":";
    private static final Set<String> OWNER_PROPERTY_NAMES = Set.of(
            "resourceOwnerUserId",
            "ownerUserId",
            "userId",
            "ownerId"
    );

    private final PermissionEvaluationService permissionEvaluationService;

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Object targetDomainObject,
            Object permission
    ) {
        Optional<String> directPermission = permissionKey(permission);
        if (directPermission.isEmpty()) {
            return false;
        }

        if (isFullPermissionKey(directPermission.get())) {
            return permissionEvaluationService.hasPermission(
                    authentication,
                    directPermission.get()
            ) || isResourceOwner(authentication, targetDomainObject);
        }

        Optional<String> targetDomain = targetDomain(targetDomainObject);
        if (targetDomain.isEmpty()) {
            return false;
        }

        return permissionEvaluationService.hasPermission(
                authentication,
                targetDomain.get(),
                directPermission.get()
        ) || isResourceOwner(authentication, targetDomainObject);
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission
    ) {
        Optional<String> action = permissionKey(permission);
        Optional<String> domain = permissionKey(targetType);
        if (action.isEmpty() || domain.isEmpty()) {
            return false;
        }

        return permissionEvaluationService.hasPermission(
                authentication,
                domain.get(),
                action.get()
        ) || isReadableUserResourceOwner(
                authentication,
                targetId,
                domain.get(),
                action.get()
        );
    }

    private boolean isResourceOwner(
            Authentication authentication,
            Object targetDomainObject
    ) {
        return resourceOwnerUserId(targetDomainObject)
                .map(ownerUserId -> permissionEvaluationService.isCurrentUser(
                        authentication,
                        ownerUserId
                ))
                .orElse(false);
    }

    private boolean isReadableUserResourceOwner(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            String action
    ) {
        if (!"user".equals(normalize(targetType)) || !"read".equals(normalize(action))) {
            return false;
        }

        return uuid(targetId)
                .map(userId -> permissionEvaluationService.isCurrentUser(
                        authentication,
                        userId
                ))
                .orElse(false);
    }

    private static Optional<String> targetDomain(Object targetDomainObject) {
        if (targetDomainObject == null) {
            return Optional.empty();
        }

        if (targetDomainObject instanceof PermissionEvaluationContext context) {
            return permissionKey(context.targetDomain());
        }

        if (targetDomainObject instanceof CharSequence value) {
            return permissionKey(value);
        }

        return permissionKey(targetDomainObject.getClass().getSimpleName());
    }

    private static Optional<UUID> resourceOwnerUserId(Object targetDomainObject) {
        if (targetDomainObject == null) {
            return Optional.empty();
        }

        if (targetDomainObject instanceof PermissionEvaluationContext context) {
            return Optional.ofNullable(context.resourceOwnerUserId());
        }

        if (targetDomainObject instanceof User user) {
            return Optional.ofNullable(user.getId());
        }

        return readOwnerProperty(targetDomainObject);
    }

    private static Optional<UUID> readOwnerProperty(Object targetDomainObject) {
        try {
            for (var descriptor : Introspector
                    .getBeanInfo(targetDomainObject.getClass(), Object.class)
                    .getPropertyDescriptors()) {
                if (!OWNER_PROPERTY_NAMES.contains(descriptor.getName())
                        || descriptor.getReadMethod() == null) {
                    continue;
                }

                return uuid(descriptor.getReadMethod().invoke(targetDomainObject));
            }
        } catch (IntrospectionException
                | IllegalAccessException
                | InvocationTargetException ignored) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    private static Optional<String> permissionKey(Object value) {
        if (!(value instanceof CharSequence charSequence)) {
            return Optional.empty();
        }

        String normalized = normalize(charSequence.toString());
        if (normalized == null) {
            return Optional.empty();
        }

        return Optional.of(normalized);
    }

    private static Optional<UUID> uuid(Object value) {
        if (value instanceof UUID uuid) {
            return Optional.of(uuid);
        }

        if (!(value instanceof CharSequence charSequence)) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(charSequence.toString().trim()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static boolean isFullPermissionKey(String permission) {
        return permission.contains(SEPARATOR);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
