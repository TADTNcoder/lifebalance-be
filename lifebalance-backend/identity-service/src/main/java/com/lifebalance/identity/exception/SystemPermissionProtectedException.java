package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class SystemPermissionProtectedException extends AppException {

    public SystemPermissionProtectedException(UUID permissionId) {
        super(
                IdentityErrorCode.SYSTEM_PERMISSION_PROTECTED,
                "System permission is protected: " + permissionId,
                HttpStatus.CONFLICT
        );
    }
}
