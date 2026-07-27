package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class RoleValidationException extends AppException {

    public RoleValidationException(String message) {
        super(
                IdentityErrorCode.ROLE_VALIDATION_FAILED,
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
