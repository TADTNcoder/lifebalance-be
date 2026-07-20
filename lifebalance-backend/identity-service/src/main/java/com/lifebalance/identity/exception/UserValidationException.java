package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserValidationException extends AppException {

    public UserValidationException(String message) {
        super(
                IdentityErrorCode.USER_VALIDATION_FAILED,
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
