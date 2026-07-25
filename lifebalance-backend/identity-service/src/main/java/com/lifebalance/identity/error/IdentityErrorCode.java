package com.lifebalance.identity.error;

public final class IdentityErrorCode {

    public static final String USER_NOT_FOUND = "IDENTITY_USER_NOT_FOUND";
    public static final String USER_VALIDATION_FAILED = "IDENTITY_USER_VALIDATION_FAILED";
    public static final String USER_EMAIL_ALREADY_EXISTS = "IDENTITY_USER_EMAIL_ALREADY_EXISTS";
    public static final String USER_USERNAME_ALREADY_EXISTS = "IDENTITY_USER_USERNAME_ALREADY_EXISTS";
    public static final String USER_ALREADY_DISABLED = "IDENTITY_USER_ALREADY_DISABLED";
    public static final String USER_ALREADY_LOCKED = "IDENTITY_USER_ALREADY_LOCKED";
    public static final String USER_NOT_LOCKED = "IDENTITY_USER_NOT_LOCKED";
    public static final String USER_SELF_LOCK_NOT_ALLOWED = "IDENTITY_USER_SELF_LOCK_NOT_ALLOWED";
    public static final String USER_ALREADY_ACTIVE = "IDENTITY_USER_ALREADY_ACTIVE";
    public static final String USER_ACTIVATION_NOT_ALLOWED = "IDENTITY_USER_ACTIVATION_NOT_ALLOWED";
    public static final String USER_ALREADY_DELETED = "IDENTITY_USER_ALREADY_DELETED";
    public static final String USER_INACTIVE = "IDENTITY_USER_INACTIVE";
    public static final String ROLE_NOT_FOUND = "IDENTITY_ROLE_NOT_FOUND";
    public static final String ROLE_VALIDATION_FAILED = "IDENTITY_ROLE_VALIDATION_FAILED";
    public static final String ROLE_CODE_ALREADY_EXISTS = "IDENTITY_ROLE_CODE_ALREADY_EXISTS";
    public static final String ROLE_NAME_ALREADY_EXISTS = "IDENTITY_ROLE_NAME_ALREADY_EXISTS";
    public static final String SYSTEM_ROLE_PROTECTED = "IDENTITY_SYSTEM_ROLE_PROTECTED";
    public static final String SYSTEM_ROLE_CREATION_NOT_ALLOWED = "IDENTITY_SYSTEM_ROLE_CREATION_NOT_ALLOWED";

    private IdentityErrorCode() {
    }
}
