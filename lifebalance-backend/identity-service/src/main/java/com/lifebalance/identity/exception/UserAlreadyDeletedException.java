package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserAlreadyDeletedException extends AppException {

    public UserAlreadyDeletedException(UUID userId) {
        super(
                IdentityErrorCode.USER_ALREADY_DELETED,
                "User already deleted: " + userId,
                HttpStatus.CONFLICT
        );
    }
}
