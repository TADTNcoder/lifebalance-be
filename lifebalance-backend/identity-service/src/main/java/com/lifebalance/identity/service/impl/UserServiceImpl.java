package com.lifebalance.identity.service.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.audit.UserAuditEventPublisher;
import com.lifebalance.identity.dto.LockUserRequest;
import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.exception.UserActivationNotAllowedException;
import com.lifebalance.identity.exception.UserAlreadyActiveException;
import com.lifebalance.identity.exception.UserAlreadyDeletedException;
import com.lifebalance.identity.exception.UserAlreadyDisabledException;
import com.lifebalance.identity.exception.UserAlreadyLockedException;
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserNotLockedException;
import com.lifebalance.identity.exception.UserSelfLockNotAllowedException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.service.UserAuthorizationCacheService;
import com.lifebalance.identity.service.UserSessionRevocationService;
import com.lifebalance.identity.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_USERNAME_LENGTH = 100;
    private static final int MAX_DISPLAY_NAME_LENGTH = 255;
    private static final int MAX_PHONE_LENGTH = 20;
    private static final int MAX_GENDER_LENGTH = 50;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final UserRepository userRepository;
    private final UserSessionRevocationService userSessionRevocationService;
    private final UserAuthorizationCacheService userAuthorizationCacheService;
    private final UserAuditEventPublisher userAuditEventPublisher;

    @Override
    public UserResponse getUserById(UUID id) {
        validateUserId(id);

        return userRepository.findById(id)
                .map(UserServiceImpl::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        validateUserId(id);
        validateRequest(request);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        User oldUser = auditCopy(user);

        applyEmailUpdate(user, request.getEmail());
        applyUsernameUpdate(user, request.getUsername());
        applyDisplayNameUpdate(user, request.getDisplayName());
        applyPhoneUpdate(user, request.getPhone());
        applyGenderUpdate(user, request.getGender());
        applyBirthDateUpdate(user, request.getBirthDate());

        User updatedUser = userRepository.save(user);
        userAuditEventPublisher.publishUserAudit(
                AuditAction.UPDATE_USER,
                oldUser,
                updatedUser,
                "User updated"
        );

        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse activateUser(UUID id) {
        User user = findExistingUser(id);

        if (user.getStatus() == AccountStatus.ACTIVE) {
            throw new UserAlreadyActiveException(id);
        }
        if (user.getStatus() != AccountStatus.INACTIVE
                && user.getStatus() != AccountStatus.DISABLED) {
            throw new UserActivationNotAllowedException(id, user.getStatus());
        }

        User oldUser = auditCopy(user);
        user.setStatus(AccountStatus.ACTIVE);

        User activatedUser = userRepository.save(user);
        userAuthorizationCacheService.evictUser(activatedUser.getId());
        userAuditEventPublisher.publishUserAudit(
                AuditAction.ACTIVATE_USER,
                oldUser,
                activatedUser,
                "User activated"
        );

        return toResponse(activatedUser);
    }

    @Override
    @Transactional
    public UserResponse disableUser(UUID id) {
        User user = findExistingUser(id);

        if (user.getStatus() == AccountStatus.DISABLED) {
            throw new UserAlreadyDisabledException(id);
        }

        User oldUser = auditCopy(user);
        OffsetDateTime disabledAt = OffsetDateTime.now();
        user.setStatus(AccountStatus.DISABLED);
        user.setTokenValidAfter(disabledAt);
        User disabledUser = userRepository.save(user);
        userSessionRevocationService.revokeSessions(disabledUser, "USER_DISABLED");
        userAuthorizationCacheService.evictUser(disabledUser.getId());
        userAuditEventPublisher.publishUserAudit(
                AuditAction.DISABLE_USER,
                oldUser,
                disabledUser,
                "User disabled and sessions revoked"
        );

        return toResponse(disabledUser);
    }

    @Override
    @Transactional
    public UserResponse lockUser(UUID id, String actorKeycloakId, LockUserRequest request) {
        validateUserId(id);
        String normalizedActorKeycloakId = validateActorKeycloakId(actorKeycloakId);
        validateLockRequest(request);

        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> resolveMissingUserException(id));

        if (user.getStatus() == AccountStatus.LOCKED) {
            throw new UserAlreadyLockedException(id);
        }
        if (normalizedActorKeycloakId.equals(normalize(user.getKeycloakId()))) {
            throw new UserSelfLockNotAllowedException(id);
        }

        User oldUser = auditCopy(user);
        OffsetDateTime lockedAt = OffsetDateTime.now();
        user.setStatus(AccountStatus.LOCKED);
        user.setLockReason(request.getReason().trim());
        user.setLockedAt(lockedAt);
        user.setLockedUntil(request.getLockedUntil());
        user.setLockedByKeycloakId(normalizedActorKeycloakId);
        user.setTokenValidAfter(lockedAt);

        User lockedUser = userRepository.save(user);
        userSessionRevocationService.revokeSessions(lockedUser, "USER_LOCKED");
        userAuthorizationCacheService.evictUser(lockedUser.getId());
        userAuditEventPublisher.publishUserAudit(
                AuditAction.LOCK_USER,
                oldUser,
                lockedUser,
                "User locked and sessions revoked"
        );

        return toResponse(lockedUser);
    }

    @Override
    @Transactional
    public UserResponse unlockUser(UUID id) {
        validateUserId(id);

        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> resolveMissingUserException(id));

        if (user.getStatus() != AccountStatus.LOCKED) {
            throw new UserNotLockedException(id);
        }

        User oldUser = auditCopy(user);
        user.setStatus(AccountStatus.ACTIVE);
        user.setLockReason(null);
        user.setLockedAt(null);
        user.setLockedUntil(null);
        user.setLockedByKeycloakId(null);

        User unlockedUser = userRepository.save(user);
        userAuthorizationCacheService.evictUser(unlockedUser.getId());
        userAuditEventPublisher.publishUserAudit(
                AuditAction.UNLOCK_USER,
                oldUser,
                unlockedUser,
                "User unlocked"
        );

        return toResponse(unlockedUser);
    }

    @Override
    @Transactional
    public void softDeleteUser(UUID id) {
        User user = findExistingUser(id);
        User oldUser = auditCopy(user);
        user.setTokenValidAfter(OffsetDateTime.now());

        userRepository.delete(user);
        userSessionRevocationService.revokeSessions(user, "USER_DELETED");
        userAuthorizationCacheService.evictUser(user.getId());
        userAuditEventPublisher.publishUserAudit(
                AuditAction.DELETE_USER,
                oldUser,
                null,
                "User deleted and sessions revoked"
        );
    }

    private User findExistingUser(UUID id) {
        validateUserId(id);

        return userRepository.findById(id)
                .orElseThrow(() -> resolveMissingUserException(id));
    }

    private RuntimeException resolveMissingUserException(UUID id) {
        if (!userRepository.existsByIdIncludingDeleted(id)) {
            return new UserNotFoundException(id);
        }
        if (userRepository.existsDeletedById(id)) {
            return new UserAlreadyDeletedException(id);
        }

        return new UserNotFoundException(id);
    }

    private void applyEmailUpdate(User user, String email) {
        if (email == null) {
            return;
        }

        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailAndIdNot(normalizedEmail, user.getId())) {
            throw new UserEmailAlreadyExistsException(normalizedEmail);
        }

        user.setEmail(normalizedEmail);
    }

    private void applyUsernameUpdate(User user, String username) {
        if (username == null) {
            return;
        }

        String normalizedUsername = normalizeUsername(username);
        if (userRepository.existsByUsernameAndIdNot(normalizedUsername, user.getId())) {
            throw new UserUsernameAlreadyExistsException(normalizedUsername);
        }

        user.setUsername(normalizedUsername);
    }

    private static void applyDisplayNameUpdate(User user, String displayName) {
        if (displayName == null) {
            return;
        }

        user.setDisplayName(displayName.trim());
    }

    private static void applyPhoneUpdate(User user, String phone) {
        if (phone != null) {
            user.setPhone(phone);
        }
    }

    private static void applyGenderUpdate(User user, String gender) {
        if (gender != null) {
            user.setGender(gender);
        }
    }

    private static void applyBirthDateUpdate(User user, LocalDate birthDate) {
        if (birthDate != null) {
            user.setBirthDate(birthDate);
        }
    }

    private static void validateUserId(UUID id) {
        if (id == null) {
            throw new UserValidationException("User id is required");
        }
    }

    private static void validateRequest(UpdateUserRequest request) {
        if (request == null) {
            throw new UserValidationException("Update request is required");
        }

        validateEmail(request.getEmail());
        validateUsername(request.getUsername());
        validateDisplayName(request.getDisplayName());
        validatePhone(request.getPhone());
        validateGender(request.getGender());
        validateBirthDate(request.getBirthDate());
    }

    private static String validateActorKeycloakId(String actorKeycloakId) {
        String normalizedActorKeycloakId = normalize(actorKeycloakId);
        if (normalizedActorKeycloakId == null) {
            throw new UserValidationException("Actor keycloak id is required");
        }

        return normalizedActorKeycloakId;
    }

    private static void validateLockRequest(LockUserRequest request) {
        if (request == null) {
            throw new UserValidationException("Lock request is required");
        }
        if (normalize(request.getReason()) == null) {
            throw new UserValidationException("Lock reason is required");
        }
        if (request.getLockedUntil() != null
                && !request.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new UserValidationException("Locked until must be in the future");
        }
    }

    private static void validateEmail(String email) {
        if (email == null) {
            return;
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            throw new UserValidationException("Email must not be blank");
        }
        if (normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new UserValidationException("Email must be at most 255 characters");
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new UserValidationException("Email must be valid");
        }
    }

    private static void validateUsername(String username) {
        if (username == null) {
            return;
        }

        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            throw new UserValidationException("Username must not be blank");
        }
        if (normalizedUsername.length() > MAX_USERNAME_LENGTH) {
            throw new UserValidationException("Username must be at most 100 characters");
        }
    }

    private static void validateDisplayName(String displayName) {
        if (displayName == null) {
            return;
        }

        String normalizedDisplayName = displayName.trim();
        if (normalizedDisplayName.isEmpty()) {
            throw new UserValidationException("Display name must not be blank");
        }
        if (normalizedDisplayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new UserValidationException("Display name must be at most 255 characters");
        }
    }

    private static void validatePhone(String phone) {
        if (phone != null && phone.trim().length() > MAX_PHONE_LENGTH) {
            throw new UserValidationException("Phone must be at most 20 characters");
        }
    }

    private static void validateGender(String gender) {
        if (gender != null && gender.trim().length() > MAX_GENDER_LENGTH) {
            throw new UserValidationException("Gender must be at most 50 characters");
        }
    }

    private static void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new UserValidationException("Birth date must not be in the future");
        }
    }

    private static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setBirthDate(user.getBirthDate());
        response.setStatus(user.getStatus());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setLockReason(user.getLockReason());
        response.setLockedAt(user.getLockedAt());
        response.setLockedUntil(user.getLockedUntil());

        return response;
    }

    private static User auditCopy(User user) {
        if (user == null) {
            return null;
        }

        User copy = new User();
        copy.setId(user.getId());
        copy.setKeycloakId(user.getKeycloakId());
        copy.setEmail(user.getEmail());
        copy.setUsername(user.getUsername());
        copy.setDisplayName(user.getDisplayName());
        copy.setPhone(user.getPhone());
        copy.setGender(user.getGender());
        copy.setBirthDate(user.getBirthDate());
        copy.setStatus(user.getStatus());
        copy.setRegisteredAt(user.getRegisteredAt());
        copy.setLastLoginAt(user.getLastLoginAt());
        copy.setLockReason(user.getLockReason());
        copy.setLockedAt(user.getLockedAt());
        copy.setLockedUntil(user.getLockedUntil());
        copy.setLockedByKeycloakId(user.getLockedByKeycloakId());
        copy.setTokenValidAfter(user.getTokenValidAfter());
        return copy;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
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
}
