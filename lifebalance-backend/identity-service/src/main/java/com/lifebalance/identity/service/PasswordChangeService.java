package com.lifebalance.identity.service;

import com.lifebalance.identity.dto.ChangePasswordRequest;
import com.lifebalance.identity.model.User;

public interface PasswordChangeService {

    Result changePassword(
            User user,
            String keycloakUsername,
            ChangePasswordRequest request,
            String clientAddress
    );

    record Result(boolean sessionsRevoked) {
    }
}
