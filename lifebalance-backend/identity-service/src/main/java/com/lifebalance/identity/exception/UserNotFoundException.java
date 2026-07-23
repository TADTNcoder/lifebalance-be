package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserNotFoundException extends AppException {

    public UserNotFoundException(UUID userId) {
        super(
                IdentityErrorCode.USER_NOT_FOUND,
                "User not found: " + userId,
                HttpStatus.NOT_FOUND
        );
    }
}
