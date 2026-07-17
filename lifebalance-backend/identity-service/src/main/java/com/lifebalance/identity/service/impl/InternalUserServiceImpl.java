package com.lifebalance.identity.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.model.User;
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
            return optionalUser.get();
        }
        User user = new User();
        user.setKeycloakId(currentUser.getUserId());
        user.setUsername(currentUser.getUsername());
        user.setEmail(currentUser.getEmail());
        return userRepository.save(user);
    }

}
