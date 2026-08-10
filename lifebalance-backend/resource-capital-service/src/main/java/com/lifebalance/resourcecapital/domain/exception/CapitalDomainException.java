package com.lifebalance.resourcecapital.domain.exception;

import com.lifebalance.common.error.AppException;
import org.springframework.http.HttpStatus;

public abstract class CapitalDomainException extends AppException {

    protected CapitalDomainException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    protected CapitalDomainException(String code, String message, HttpStatus status, Throwable cause) {
        super(code, message, status, cause);
    }
}
