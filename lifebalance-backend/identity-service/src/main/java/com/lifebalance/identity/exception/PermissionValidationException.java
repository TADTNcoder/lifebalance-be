package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class PermissionValidationException extends AppException {

    public PermissionValidationException(String message) {
        super(
                IdentityErrorCode.PERMISSION_VALIDATION_FAILED,
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
