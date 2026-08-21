package com.lifebalance.analytics.error;

import com.lifebalance.common.error.AppException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public final class AnalyticsExceptions {

    private AnalyticsExceptions() {
    }

    public static AppException actualRecordNotFound(UUID actualRecordId) {
        return notFound(AnalyticsErrorCode.ACTUAL_RECORD_NOT_FOUND, "Actual record was not found",
                Map.of("actualRecordId", String.valueOf(actualRecordId)));
    }

    public static AppException evaluationNotFound(UUID evaluationId) {
        return notFound(AnalyticsErrorCode.EVALUATION_NOT_FOUND, "Evaluation result was not found",
                Map.of("evaluationId", String.valueOf(evaluationId)));
    }

    public static AppException reportNotFound(UUID reportId) {
        return notFound(AnalyticsErrorCode.REPORT_NOT_FOUND, "Analytics report was not found",
                Map.of("reportId", String.valueOf(reportId)));
    }

    public static AppException invalidRequest(String reason) {
        return badRequest(AnalyticsErrorCode.INVALID_REQUEST, "Analytics request is invalid",
                Map.of("reason", reason));
    }

    public static AppException invalidState(UUID entityId, String status) {
        return new AppException(
                AnalyticsErrorCode.INVALID_STATE,
                "Analytics entity state does not allow this operation",
                HttpStatus.CONFLICT,
                Map.of(
                        "entityId", String.valueOf(entityId),
                        "status", status
                )
        );
    }

    public static AppException invalidPeriod(String reason) {
        return badRequest(AnalyticsErrorCode.INVALID_PERIOD, "Analytics period is invalid",
                Map.of("reason", reason));
    }

    public static AppException invalidCurrency(String currencyCode) {
        return badRequest(AnalyticsErrorCode.INVALID_CURRENCY, "Currency code is invalid",
                Map.of("currencyCode", String.valueOf(currencyCode)));
    }

    public static AppException textTooLong(int maxLength) {
        return badRequest(AnalyticsErrorCode.TEXT_TOO_LONG, "Analytics text exceeds the maximum length",
                Map.of("maxLength", String.valueOf(maxLength)));
    }

    private static AppException notFound(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.NOT_FOUND, details);
    }

    private static AppException badRequest(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.BAD_REQUEST, details);
    }
}
