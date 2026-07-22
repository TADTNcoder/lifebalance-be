package com.lifebalance.identity.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserAlreadyDisabledException extends AppException {

    public UserAlreadyDisabledException(UUID userId) {
        super(
                IdentityErrorCode.USER_ALREADY_DISABLED,
                "User already disabled: " + userId,
                HttpStatus.CONFLICT
        );
    }
}
