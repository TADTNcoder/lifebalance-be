package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import org.springframework.http.HttpStatus;

public class RoleNameAlreadyExistsException extends AppException {

    public RoleNameAlreadyExistsException(String name) {
        super(
                IdentityErrorCode.ROLE_NAME_ALREADY_EXISTS,
                "Role name already exists: " + name,
                HttpStatus.CONFLICT
        );
    }
}
