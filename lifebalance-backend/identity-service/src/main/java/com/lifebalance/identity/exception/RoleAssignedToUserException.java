package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class RoleAssignedToUserException extends AppException {

    public RoleAssignedToUserException(UUID roleId) {
        super(
                IdentityErrorCode.ROLE_ASSIGNED_TO_USER,
                "Role is assigned to at least one user: " + roleId,
                HttpStatus.CONFLICT
        );
    }
}
