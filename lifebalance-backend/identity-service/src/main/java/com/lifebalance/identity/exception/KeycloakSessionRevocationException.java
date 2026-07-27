package com.lifebalance.identity.exception;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;

public class KeycloakSessionRevocationException extends AppException {

    public KeycloakSessionRevocationException(String message) {
        super(
                IdentityErrorCode.KEYCLOAK_SESSION_REVOCATION_FAILED,
                message,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
