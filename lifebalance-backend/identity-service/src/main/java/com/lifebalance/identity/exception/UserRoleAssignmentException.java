package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class UserRoleAssignmentException extends AppException {

    private UserRoleAssignmentException(String code, String message, UUID userId, UUID roleId) {
        super(
                code,
                message,
                HttpStatus.CONFLICT,
                Map.of(
                        "userId", String.valueOf(userId),
                        "roleId", String.valueOf(roleId)
                )
        );
    }

    public static UserRoleAssignmentException alreadyAssigned(UUID userId, UUID roleId) {
        return new UserRoleAssignmentException(
                IdentityErrorCode.ROLE_ALREADY_ASSIGNED_TO_USER,
                "Role " + roleId + " is already assigned to user " + userId + ".",
                userId,
                roleId
        );
    }

    public static UserRoleAssignmentException notAssigned(UUID userId, UUID roleId) {
        return new UserRoleAssignmentException(
                IdentityErrorCode.ROLE_NOT_ASSIGNED_TO_USER,
                "Role " + roleId + " is not assigned to user " + userId + ".",
                userId,
                roleId
        );
    }
}
