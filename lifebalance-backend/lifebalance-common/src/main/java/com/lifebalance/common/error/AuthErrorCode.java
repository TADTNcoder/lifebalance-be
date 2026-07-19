package com.lifebalance.common.error;

public final class AuthErrorCode {

    public static final String UNAUTHORIZED = "AUTH_UNAUTHORIZED";
    public static final String FORBIDDEN = "AUTH_FORBIDDEN";
    public static final String INVALID_TOKEN = "AUTH_INVALID_TOKEN";
    public static final String EXPIRED_TOKEN = "AUTH_EXPIRED_TOKEN";

    private AuthErrorCode() {
    }

}