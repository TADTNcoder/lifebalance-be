package com.lifebalance.common.error;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Map<String, String> details;

    public AppException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = Map.of();
    }

    public AppException(String code, String message, HttpStatus status, Map<String, String> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public AppException(String code, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
        this.details = Map.of();
    }

    public AppException(
            String code,
            String message,
            HttpStatus status,
            Map<String, String> details,
            Throwable cause
    ) {
        super(message, cause);
        this.code = code;
        this.status = status;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getDetails() {
        return details;
    }

}
