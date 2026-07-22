package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.model.enums.AccountStatus;

public class UserInactiveException extends AppException {

    public UserInactiveException(AccountStatus status) {
        super(
                IdentityErrorCode.USER_INACTIVE,
                "User account is not active: " + status,
                HttpStatus.FORBIDDEN
        );
    }
}
