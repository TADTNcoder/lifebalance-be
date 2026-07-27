package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class PermissionAlreadyExistsException extends AppException {

    public PermissionAlreadyExistsException(String code) {
        super(
                IdentityErrorCode.PERMISSION_ALREADY_EXISTS,
                "Permission code already exists: " + code,
                HttpStatus.CONFLICT
        );
    }
}
