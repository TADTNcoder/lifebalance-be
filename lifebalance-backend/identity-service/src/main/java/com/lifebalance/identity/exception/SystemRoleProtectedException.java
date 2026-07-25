package com.lifebalance.identity.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class SystemRoleProtectedException extends AppException {

    public SystemRoleProtectedException(UUID roleId) {
        super(
                IdentityErrorCode.SYSTEM_ROLE_PROTECTED,
                "System role is protected: " + roleId,
                HttpStatus.CONFLICT
        );
    }
}
