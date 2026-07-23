package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserAlreadyActiveException extends AppException {

    public UserAlreadyActiveException(UUID userId) {
        super(
                IdentityErrorCode.USER_ALREADY_ACTIVE,
                "User already active: " + userId,
                HttpStatus.CONFLICT
        );
    }
}
