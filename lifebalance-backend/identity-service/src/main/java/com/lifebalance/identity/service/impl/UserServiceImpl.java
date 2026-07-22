package com.lifebalance.identity.service.impl;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.exception.UserActivationNotAllowedException;
import com.lifebalance.identity.exception.UserAlreadyActiveException;
import com.lifebalance.identity.exception.UserAlreadyDeletedException;
import com.lifebalance.identity.exception.UserAlreadyDisabledException;
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
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
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final UserRepository userRepository;
    private final UserSessionRevocationService userSessionRevocationService;

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

        applyEmailUpdate(user, request.getEmail());
        applyUsernameUpdate(user, request.getUsername());
        applyDisplayNameUpdate(user, request.getDisplayName());

        return toResponse(userRepository.save(user));
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

        user.setStatus(AccountStatus.ACTIVE);

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse disableUser(UUID id) {
        User user = findExistingUser(id);

        if (user.getStatus() == AccountStatus.DISABLED) {
            throw new UserAlreadyDisabledException(id);
        }

        user.setStatus(AccountStatus.DISABLED);
        User disabledUser = userRepository.save(user);
        userSessionRevocationService.revokeSessions(disabledUser, "USER_DISABLED");

        return toResponse(disabledUser);
    }

    @Override
    @Transactional
    public void softDeleteUser(UUID id) {
        User user = findExistingUser(id);

        userRepository.delete(user);
        userSessionRevocationService.revokeSessions(user, "USER_DELETED");
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

    private static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setStatus(user.getStatus());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
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
