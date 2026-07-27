package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserNotLockedException extends AppException {

    public UserNotLockedException(UUID userId) {
        super(
                IdentityErrorCode.USER_NOT_LOCKED,
                "User is not locked: " + userId,
                HttpStatus.CONFLICT
        );
    }
}
