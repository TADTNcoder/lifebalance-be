package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class RoleCodeAlreadyExistsException extends AppException {

    public RoleCodeAlreadyExistsException(String code) {
        super(
                IdentityErrorCode.ROLE_CODE_ALREADY_EXISTS,
                "Role code already exists: " + code,
                HttpStatus.CONFLICT
        );
    }
}
