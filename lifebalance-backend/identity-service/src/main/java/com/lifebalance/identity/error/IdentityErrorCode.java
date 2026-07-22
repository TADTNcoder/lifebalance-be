package com.lifebalance.identity.error;

public final class IdentityErrorCode {

    public static final String USER_NOT_FOUND = "IDENTITY_USER_NOT_FOUND";
    public static final String USER_VALIDATION_FAILED = "IDENTITY_USER_VALIDATION_FAILED";
    public static final String USER_EMAIL_ALREADY_EXISTS = "IDENTITY_USER_EMAIL_ALREADY_EXISTS";
    public static final String USER_USERNAME_ALREADY_EXISTS = "IDENTITY_USER_USERNAME_ALREADY_EXISTS";
    public static final String USER_ALREADY_DISABLED = "IDENTITY_USER_ALREADY_DISABLED";
    public static final String USER_ALREADY_DELETED = "IDENTITY_USER_ALREADY_DELETED";
    public static final String USER_INACTIVE = "IDENTITY_USER_INACTIVE";

    private IdentityErrorCode() {
    }
}
