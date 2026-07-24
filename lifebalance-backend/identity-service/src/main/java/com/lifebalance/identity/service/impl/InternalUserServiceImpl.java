package com.lifebalance.identity.service.impl;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.lifebalance.identity.dto.UpdateUserRequest;
<<<<<<< HEAD
import com.lifebalance.identity.dto.UserResponse;
=======
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
>>>>>>> origin/main
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.InternalUserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InternalUserServiceImpl implements InternalUserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public User findOrCreate(CurrentUser currentUser) {
        validateCurrentUser(currentUser);

        String keycloakId = normalize(currentUser.getUserId());
        Optional<User> optionalUser = userRepository.findByKeycloakId(keycloakId);

        if (optionalUser.isPresent()) {
            User user = requireActive(optionalUser.get());
            return syncIdentityClaims(user, currentUser);
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
        return userRepository.save(user);
    }

    @Override
    public User getCurrentUser(CurrentUser currentUser) {
        return userRepository.findByKeycloakId(currentUser.getUserId())
                .map(InternalUserServiceImpl::requireActive)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    @Override
    public User updateCurrentUser(CurrentUser currentUser, UpdateUserRequest request) {
        User user = userRepository.findByKeycloakId(currentUser.getUserId())
                .map(InternalUserServiceImpl::requireActive)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

    private static void validateCurrentUser(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new UserValidationException("Current user is required");
        }
        if (normalize(currentUser.getUserId()) == null) {
            throw new UserValidationException("Keycloak subject is required");
        }
        if (normalizeEmail(currentUser.getEmail()) == null) {
            throw new UserValidationException("Email is required");
        }
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
