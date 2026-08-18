package com.lifebalance.resourcecapital.domain.exception;

import com.lifebalance.common.error.AppException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public abstract class CapitalDomainException extends AppException {

    protected CapitalDomainException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    protected CapitalDomainException(String code, String message, HttpStatus status, Map<String, String> details) {
        super(code, message, status, details);
    }

    protected CapitalDomainException(String code, String message, HttpStatus status, Throwable cause) {
        super(code, message, status, cause);
    }

    protected CapitalDomainException(
            String code,
            String message,
            HttpStatus status,
            Map<String, String> details,
            Throwable cause
    ) {
        super(code, message, status, details, cause);
    }
}
