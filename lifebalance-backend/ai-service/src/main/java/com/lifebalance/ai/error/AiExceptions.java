package com.lifebalance.ai.error;

import com.lifebalance.common.error.AppException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public final class AiExceptions {

    private AiExceptions() {
    }

    public static AppException conversationNotFound(UUID conversationId) {
        return notFound(AiErrorCode.CONVERSATION_NOT_FOUND, "AI conversation was not found",
                Map.of("conversationId", String.valueOf(conversationId)));
    }

    public static AppException messageNotFound(UUID messageId) {
        return notFound(AiErrorCode.MESSAGE_NOT_FOUND, "AI message was not found",
                Map.of("messageId", String.valueOf(messageId)));
    }

    public static AppException recommendationNotFound(UUID recommendationId) {
        return notFound(AiErrorCode.RECOMMENDATION_NOT_FOUND, "AI recommendation was not found",
                Map.of("recommendationId", String.valueOf(recommendationId)));
    }

    public static AppException insightNotFound(UUID insightId) {
        return notFound(AiErrorCode.INSIGHT_NOT_FOUND, "AI insight was not found",
                Map.of("insightId", String.valueOf(insightId)));
    }

    public static AppException invalidRequest(String reason) {
        return badRequest(AiErrorCode.INVALID_REQUEST, "AI request is invalid",
                Map.of("reason", reason));
    }

    public static AppException invalidState(UUID entityId, String status) {
        return new AppException(
                AiErrorCode.INVALID_STATE,
                "AI entity state does not allow this operation",
                HttpStatus.CONFLICT,
                Map.of(
                        "entityId", String.valueOf(entityId),
                        "status", status
                )
        );
    }

    public static AppException invalidPeriod(String reason) {
        return badRequest(AiErrorCode.INVALID_PERIOD, "AI period is invalid", Map.of("reason", reason));
    }

    public static AppException textTooLong(int maxLength) {
        return badRequest(AiErrorCode.TEXT_TOO_LONG, "AI text exceeds the maximum length",
                Map.of("maxLength", String.valueOf(maxLength)));
    }

    private static AppException notFound(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.NOT_FOUND, details);
    }

    private static AppException badRequest(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.BAD_REQUEST, details);
    }
}
