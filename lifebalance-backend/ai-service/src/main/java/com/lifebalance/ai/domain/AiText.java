package com.lifebalance.ai.domain;

import com.lifebalance.ai.error.AiExceptions;

public final class AiText {

    private AiText() {
    }

    public static String normalize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw AiExceptions.textTooLong(maxLength);
        }
        return normalized;
    }

    public static String require(String value, int maxLength, String message) {
        String normalized = normalize(value, maxLength);
        if (normalized == null) {
            throw AiExceptions.invalidRequest(message);
        }
        return normalized;
    }
}
