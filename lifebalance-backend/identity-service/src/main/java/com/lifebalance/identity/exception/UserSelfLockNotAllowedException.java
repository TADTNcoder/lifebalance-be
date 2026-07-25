package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserSelfLockNotAllowedException extends AppException {

    public UserSelfLockNotAllowedException(UUID userId) {
        super(
                IdentityErrorCode.USER_SELF_LOCK_NOT_ALLOWED,
                "User cannot lock own account: " + userId,
                HttpStatus.CONFLICT
        );
    }
}
