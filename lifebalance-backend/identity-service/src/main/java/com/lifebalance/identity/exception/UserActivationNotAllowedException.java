package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.model.enums.AccountStatus;

public class UserActivationNotAllowedException extends AppException {

    public UserActivationNotAllowedException(UUID userId, AccountStatus status) {
        super(
                IdentityErrorCode.USER_ACTIVATION_NOT_ALLOWED,
                "User cannot be activated from status " + status + ": " + userId,
                HttpStatus.CONFLICT
        );
    }
}
