package com.lifebalance.identity.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.exception.UserInactiveException;
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
        Optional<User> optionalUser = userRepository.findByKeycloakId(currentUser.getUserId());

        if (optionalUser.isPresent()) {
            return requireActive(optionalUser.get());
        }
        if (userRepository.existsDeletedByKeycloakId(currentUser.getUserId())) {
            throw new UserInactiveException(AccountStatus.DELETED);
        }
        User user = new User();
        user.setKeycloakId(currentUser.getUserId());
        user.setUsername(currentUser.getUsername());
        user.setEmail(currentUser.getEmail());
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

    private static User requireActive(User user) {
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UserInactiveException(user.getStatus());
        }

        return user;
    }

}
