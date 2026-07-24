package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserAlreadyLockedException extends AppException {

    public UserAlreadyLockedException(UUID userId) {
        super(
                IdentityErrorCode.USER_ALREADY_LOCKED,
                "User already locked: " + userId,
                HttpStatus.CONFLICT
        );
    }
}
