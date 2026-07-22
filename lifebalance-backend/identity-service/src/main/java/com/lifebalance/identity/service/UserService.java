package com.lifebalance.identity.service;

import java.util.UUID;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;

public interface UserService {

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    UserResponse activateUser(UUID id);

    UserResponse disableUser(UUID id);

    void softDeleteUser(UUID id);
}
