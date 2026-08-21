package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class RolePermissionAssignmentException extends AppException {

    private RolePermissionAssignmentException(String code, String message, UUID roleId, UUID permissionId) {
        super(
                code,
                message,
                HttpStatus.CONFLICT,
                Map.of(
                        "roleId", String.valueOf(roleId),
                        "permissionId", String.valueOf(permissionId)
                )
        );
    }

    public static RolePermissionAssignmentException alreadyAssigned(UUID roleId, UUID permissionId) {
        return new RolePermissionAssignmentException(
                IdentityErrorCode.PERMISSION_ALREADY_ASSIGNED_TO_ROLE,
                "Permission " + permissionId + " is already assigned to role " + roleId + ".",
                roleId,
                permissionId
        );
    }

    public static RolePermissionAssignmentException notAssigned(UUID roleId, UUID permissionId) {
        return new RolePermissionAssignmentException(
                IdentityErrorCode.PERMISSION_NOT_ASSIGNED_TO_ROLE,
                "Permission " + permissionId + " is not assigned to role " + roleId + ".",
                roleId,
                permissionId
        );
    }
}
