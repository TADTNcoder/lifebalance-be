package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class SystemPermissionCreationNotAllowedException extends AppException {

    public SystemPermissionCreationNotAllowedException() {
        super(
                IdentityErrorCode.SYSTEM_PERMISSION_CREATION_NOT_ALLOWED,
                "System permission creation is not allowed",
                HttpStatus.CONFLICT
        );
    }
}
