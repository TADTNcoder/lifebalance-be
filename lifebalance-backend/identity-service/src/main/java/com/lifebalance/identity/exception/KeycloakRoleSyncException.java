package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class KeycloakRoleSyncException extends AppException {

    public KeycloakRoleSyncException(String message) {
        super(
                IdentityErrorCode.KEYCLOAK_ROLE_SYNC_FAILED,
                message,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
