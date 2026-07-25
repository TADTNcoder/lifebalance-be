package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends AppException {

    public RoleNotFoundException(UUID roleId) {
        super(
                IdentityErrorCode.ROLE_NOT_FOUND,
                "Role not found: " + roleId,
                HttpStatus.NOT_FOUND
        );
    }
}
