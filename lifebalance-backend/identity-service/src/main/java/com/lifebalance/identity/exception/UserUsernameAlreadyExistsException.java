package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserUsernameAlreadyExistsException extends AppException {

    public UserUsernameAlreadyExistsException(String username) {
        super(
                IdentityErrorCode.USER_USERNAME_ALREADY_EXISTS,
                "Username already exists: " + username,
                HttpStatus.CONFLICT
        );
    }
}
