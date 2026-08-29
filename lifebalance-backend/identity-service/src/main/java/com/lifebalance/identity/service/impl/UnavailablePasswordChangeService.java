package com.lifebalance.identity.service.impl;

import com.lifebalance.identity.dto.ChangePasswordRequest;
import com.lifebalance.identity.error.PasswordChangeExceptions;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.PasswordChangeService;

public class UnavailablePasswordChangeService implements PasswordChangeService {

    @Override
    public Result changePassword(
            User user,
            String keycloakUsername,
            ChangePasswordRequest request,
            String clientAddress
    ) {
        throw PasswordChangeExceptions.unavailable();
    }
}
