package com.lifebalance.common.error;

import com.lifebalance.common.api.ApiError;
import com.lifebalance.common.api.ApiResponse;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final SecurityExceptionAuditLogger securityExceptionAuditLogger;

    public GlobalExceptionHandler() {
        this(new SecurityExceptionAuditLogger());
    }

    GlobalExceptionHandler(SecurityExceptionAuditLogger securityExceptionAuditLogger) {
        this.securityExceptionAuditLogger = securityExceptionAuditLogger;
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ApiError error = ApiError.of(exception.getCode(), exception.getMessage(), exception.getDetails());
        return ResponseEntity.status(exception.getStatus()).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null
                                ? "Invalid value"
                                : fieldError.getDefaultMessage(),
                        (first, ignored) -> first
                ));

        ApiError error = ApiError.of(
                CommonErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        Class<?> requiredType = exception.getRequiredType();
        String expectedType = requiredType == null ? "expected type" : requiredType.getSimpleName();

        ApiError error = ApiError.of(
                CommonErrorCode.VALIDATION_FAILED,
                "Request parameter has invalid format",
                Map.of(
                        exception.getName(),
                        "must be a valid " + expectedType
                )
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleMessageNotReadableException(HttpMessageNotReadableException exception) {
        ApiError error = ApiError.of(
                CommonErrorCode.VALIDATION_FAILED,
                "Request body has invalid format",
                Map.of("body", "must be valid JSON with supported field values")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        ApiError error = ApiError.of(AuthErrorCode.UNAUTHORIZED, "Authentication is required");
        securityExceptionAuditLogger.logAuthenticationFailure(exception, error.code());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        ApiError error = ApiError.of(AuthErrorCode.FORBIDDEN, "Access is denied");
        securityExceptionAuditLogger.logAuthorizationFailure(exception, error.code());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse
                && errorResponse.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            ApiError error = ApiError.of(CommonErrorCode.NOT_FOUND, "Resource was not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(error));
        }

        ApiError error = ApiError.of(CommonErrorCode.INTERNAL_ERROR, "Unexpected server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(error));
    }

}
