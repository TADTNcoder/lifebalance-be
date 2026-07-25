package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class PermissionNotFoundException extends AppException {

    public PermissionNotFoundException(UUID permissionId) {
        super(
                IdentityErrorCode.PERMISSION_NOT_FOUND,
                "Permission not found: " + permissionId,
                HttpStatus.NOT_FOUND
        );
    }

    public PermissionNotFoundException(String code) {
        super(
                IdentityErrorCode.PERMISSION_NOT_FOUND,
                "Permission not found: " + code,
                HttpStatus.NOT_FOUND
        );
    }
}
