package com.lifebalance.identity.error;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;

public final class PasswordChangeExceptions {

    private PasswordChangeExceptions() {
    }

    public static AppException invalidCurrentPassword() {
        return new AppException(
                IdentityErrorCode.CURRENT_PASSWORD_INVALID,
                "Current password is incorrect",
                HttpStatus.BAD_REQUEST,
                Map.of("currentPassword", "Current password is incorrect")
        );
    }

    public static AppException confirmationMismatch() {
        return new AppException(
                IdentityErrorCode.PASSWORD_CONFIRMATION_MISMATCH,
                "Password confirmation does not match",
                HttpStatus.BAD_REQUEST,
                Map.of("confirmPassword", "Password confirmation does not match")
        );
    }

    public static AppException samePassword() {
        return policyViolation("New password must be different from the current password");
    }

    public static AppException policyViolation(String message) {
        return new AppException(
                IdentityErrorCode.PASSWORD_POLICY_VIOLATION,
                "New password does not meet the password policy",
                HttpStatus.BAD_REQUEST,
                Map.of("newPassword", message)
        );
    }

    public static AppException rateLimited(long retryAfterSeconds) {
        return new AppException(
                IdentityErrorCode.PASSWORD_CHANGE_RATE_LIMITED,
                "Too many password verification attempts",
                HttpStatus.TOO_MANY_REQUESTS,
                Map.of("retryAfterSeconds", Long.toString(Math.max(1L, retryAfterSeconds)))
        );
    }

    public static AppException unavailable() {
        return new AppException(
                IdentityErrorCode.PASSWORD_CHANGE_UNAVAILABLE,
                "Password change service is temporarily unavailable",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static AppException keycloakUserNotFound() {
        return new AppException(
                IdentityErrorCode.USER_NOT_FOUND,
                "Authenticated user was not found in Keycloak",
                HttpStatus.NOT_FOUND
        );
    }
}
