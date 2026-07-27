package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class PermissionAssignedToRoleException extends AppException {

    public PermissionAssignedToRoleException(UUID permissionId) {
        super(
                IdentityErrorCode.PERMISSION_ASSIGNED_TO_ROLE,
                "Permission is assigned to at least one role: " + permissionId,
                HttpStatus.CONFLICT
        );
    }
}
