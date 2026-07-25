package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class SystemRoleCreationNotAllowedException extends AppException {

    public SystemRoleCreationNotAllowedException() {
        super(
                IdentityErrorCode.SYSTEM_ROLE_CREATION_NOT_ALLOWED,
                "System role creation is not allowed",
                HttpStatus.CONFLICT
        );
    }
}
