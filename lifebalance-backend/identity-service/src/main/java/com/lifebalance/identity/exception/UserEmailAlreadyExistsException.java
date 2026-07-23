package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserEmailAlreadyExistsException extends AppException {

    public UserEmailAlreadyExistsException(String email) {
        super(
                IdentityErrorCode.USER_EMAIL_ALREADY_EXISTS,
                "Email already exists: " + email,
                HttpStatus.CONFLICT
        );
    }
}
