package com.lifebalance.identity.service;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalUserService {
    User findOrCreate(CurrentUser currentUser);

    User getCurrentUser(CurrentUser currentUser);

    User updateCurrentUser(CurrentUser currentUser, UpdateUserRequest updateUserRequest);

    Page<UserResponse> search(
            String keyword,
            Pageable pageable);
}
