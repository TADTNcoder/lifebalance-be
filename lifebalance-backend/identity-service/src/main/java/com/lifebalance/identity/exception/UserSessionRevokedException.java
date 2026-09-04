package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class UserSessionRevokedException extends AppException {

    public UserSessionRevokedException() {
        super(
                IdentityErrorCode.USER_SESSION_REVOKED,
                "User session has been revoked",
                HttpStatus.UNAUTHORIZED
        );
    }
}
