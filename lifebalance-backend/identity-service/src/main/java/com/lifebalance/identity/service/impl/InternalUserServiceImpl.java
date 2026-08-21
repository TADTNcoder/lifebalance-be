package com.lifebalance.identity.service.impl;

import java.util.Locale;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.repository.UserRoleRepository;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.UserAuthorizationCacheService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InternalUserServiceImpl implements InternalUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserAuthorizationCacheService userAuthorizationCacheService;

    @Transactional
    @Override
    public User findOrCreate(CurrentUser currentUser) {
        validateCurrentUser(currentUser);

        String keycloakId = normalize(currentUser.getUserId());
        Optional<User> optionalUser = userRepository.findByKeycloakId(keycloakId);

        if (optionalUser.isPresent()) {
            User user = requireActive(optionalUser.get());
            user = syncIdentityClaims(user, currentUser);
            syncRolesFromToken(user, currentUser.getRoles());
            return user;
        }
        if (userRepository.existsDeletedByKeycloakId(keycloakId)) {
            throw new UserInactiveException(AccountStatus.DELETED);
        }

        String email = normalizeEmail(currentUser.getEmail());
        String username = normalizeUsername(currentUser.getUsername());
        if (userRepository.existsByEmail(email)) {
            throw new UserEmailAlreadyExistsException(email);
        }
        if (username != null && userRepository.existsByUsername(username)) {
            throw new UserUsernameAlreadyExistsException(username);
        }

        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setUsername(username);
        user.setEmail(email);
        user = userRepository.save(user);
        syncRolesFromToken(user, currentUser.getRoles());
        return user;
    }

    @Override
    public User getCurrentUser(CurrentUser currentUser) {
        String keycloakId = requireKeycloakSubject(currentUser);

        return userRepository.findByKeycloakId(keycloakId)
                .map(InternalUserServiceImpl::requireActive)
                .orElseThrow(() -> new UserNotFoundException(keycloakId));
    }

    @Transactional
    @Override
    public User updateCurrentUser(CurrentUser currentUser, UpdateUserRequest request) {
        String keycloakId = requireKeycloakSubject(currentUser);

        User user = userRepository.findByKeycloakId(keycloakId)
                .map(InternalUserServiceImpl::requireActive)
                .orElseThrow(() -> new UserNotFoundException(keycloakId));

        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }

    @Override
    public Page<UserResponse> search(
            String keyword,
            Pageable pageable) {

        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable)
                .map(this::mapToResponse);
    }

    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setStatus(user.getStatus());
        response.setLockReason(user.getLockReason());
        response.setLockedAt(user.getLockedAt());
        response.setLockedUntil(user.getLockedUntil());

        return response;
    }

    private static User requireActive(User user) {
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UserInactiveException(user.getStatus());
        }

        return user;
    }

    private User syncIdentityClaims(User user, CurrentUser currentUser) {
        boolean changed = false;

        String email = normalizeEmail(currentUser.getEmail());
        if (!Objects.equals(user.getEmail(), email)) {
            if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
                throw new UserEmailAlreadyExistsException(email);
            }
            user.setEmail(email);
            changed = true;
        }

        String username = normalizeUsername(currentUser.getUsername());
        if (!Objects.equals(user.getUsername(), username)) {
            if (username != null && userRepository.existsByUsernameAndIdNot(username, user.getId())) {
                throw new UserUsernameAlreadyExistsException(username);
            }
            user.setUsername(username);
            changed = true;
        }

        return changed ? userRepository.save(user) : user;
    }

    private void syncRolesFromToken(User user, Collection<String> tokenRoles) {
        if (user.getId() == null) {
            return;
        }

        Set<String> normalizedTokenRoles = normalizeRoles(tokenRoles);
        if (normalizedTokenRoles.isEmpty()) {
            return;
        }

        List<Role> matchedRoles = roleRepository.findByCodesIgnoreCase(normalizedTokenRoles);
        Map<UUID, Role> targetRolesById = matchedRoles.stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));
        List<UserRole> existingUserRoles = userRoleRepository.findByUserId(user.getId());
        Set<UUID> existingRoleIds = existingUserRoles.stream()
                .map(userRole -> userRole.getRole().getId())
                .collect(Collectors.toSet());

        List<UUID> staleRoleIds = existingUserRoles.stream()
                .filter(userRole -> !targetRolesById.containsKey(userRole.getRole().getId()))
                .map(userRole -> userRole.getRole().getId())
                .toList();
        if (!staleRoleIds.isEmpty()) {
            userRoleRepository.deleteByUserIdAndRoleIds(user.getId(), staleRoleIds);
        }

        OffsetDateTime assignedAt = OffsetDateTime.now();
        List<UserRole> missingUserRoles = targetRolesById.values().stream()
                .filter(role -> !existingRoleIds.contains(role.getId()))
                .map(role -> UserRole.builder()
                        .id(new UserRoleId(user.getId(), role.getId()))
                        .user(user)
                        .role(role)
                        .assignedAt(assignedAt)
                        .build())
                .toList();
        if (!missingUserRoles.isEmpty()) {
            userRoleRepository.saveAll(missingUserRoles);
        }

        if (!staleRoleIds.isEmpty() || !missingUserRoles.isEmpty()) {
            userAuthorizationCacheService.evictUser(user.getId());
        }
    }

    private static Set<String> normalizeRoles(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .map(InternalUserServiceImpl::normalizeRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String normalizeRoleCode(String role) {
        String normalized = normalize(role);
        if (normalized == null) {
            return null;
        }

        String lowerCaseRole = normalized.toLowerCase(Locale.ROOT);
        if (lowerCaseRole.startsWith("role_") || lowerCaseRole.startsWith("role-")) {
            return lowerCaseRole.substring(5);
        }

        return lowerCaseRole;
    }

    private static void validateCurrentUser(CurrentUser currentUser) {
        requireKeycloakSubject(currentUser);
        if (normalizeEmail(currentUser.getEmail()) == null) {
            throw new UserValidationException("Email is required");
        }
    }

    private static String requireKeycloakSubject(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new UserValidationException("Current user is required");
        }

        String keycloakId = normalize(currentUser.getUserId());
        if (keycloakId == null) {
            throw new UserValidationException("Keycloak subject is required");
        }

        return keycloakId;
    }

    private static String normalizeEmail(String email) {
        String normalizedEmail = normalize(email);
        return normalizedEmail == null
                ? null
                : normalizedEmail.toLowerCase(Locale.ROOT);
    }

    private static String normalizeUsername(String username) {
        String normalizedUsername = normalize(username);
        return normalizedUsername == null
                ? null
                : normalizedUsername.toLowerCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
