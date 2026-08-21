package com.lifebalance.notification.error;

import com.lifebalance.common.error.AppException;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationStatus;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public final class NotificationExceptions {

    private NotificationExceptions() {
    }

    public static AppException notificationNotFound(UUID notificationId) {
        return notFound(NotificationErrorCode.NOTIFICATION_NOT_FOUND, "Notification was not found",
                Map.of("notificationId", String.valueOf(notificationId)));
    }

    public static AppException policyNotApproved(NotificationEventType eventType) {
        return new AppException(
                NotificationErrorCode.NOTIFICATION_POLICY_NOT_APPROVED,
                "Notification policy requires explicit approval before creating this notification",
                HttpStatus.CONFLICT,
                Map.of(
                        "eventType", String.valueOf(eventType),
                        "confirmationField", "policyApproved",
                        "confirmationRequired", "true"
                )
        );
    }

    public static AppException invalidRequest(String reason) {
        return badRequest(NotificationErrorCode.NOTIFICATION_INVALID_REQUEST, "Notification request is invalid",
                Map.of("reason", reason));
    }

    public static AppException invalidReference() {
        return badRequest(NotificationErrorCode.NOTIFICATION_INVALID_REFERENCE,
                "Notification referenceType and referenceId must be provided together");
    }

    public static AppException invalidState(UUID notificationId, NotificationStatus status) {
        return conflict(NotificationErrorCode.NOTIFICATION_INVALID_STATE,
                "Notification state does not allow this operation",
                Map.of(
                        "notificationId", String.valueOf(notificationId),
                        "status", String.valueOf(status)
                ));
    }

    public static AppException invalidDeliveryState(
            UUID notificationId,
            NotificationDeliveryStatus actual,
            NotificationDeliveryStatus expected
    ) {
        return conflict(NotificationErrorCode.NOTIFICATION_DELIVERY_INVALID_STATE,
                "Notification delivery state does not allow this operation",
                Map.of(
                        "notificationId", String.valueOf(notificationId),
                        "actualStatus", String.valueOf(actual),
                        "expectedStatus", String.valueOf(expected)
                ));
    }

    public static AppException preferenceNotFound(UUID preferenceId) {
        return notFound(NotificationErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND,
                "Notification preference was not found",
                Map.of("preferenceId", String.valueOf(preferenceId)));
    }

    public static AppException templateNotFound(UUID templateId) {
        return notFound(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND,
                "Notification template was not found",
                Map.of("templateId", String.valueOf(templateId)));
    }

    public static AppException templateAlreadyExists(String templateKey) {
        return conflict(NotificationErrorCode.NOTIFICATION_TEMPLATE_ALREADY_EXISTS,
                "Notification template already exists for this channel",
                Map.of("templateKey", templateKey));
    }

    public static AppException textTooLong(int maxLength) {
        return badRequest(NotificationErrorCode.NOTIFICATION_TEXT_TOO_LONG,
                "Notification text exceeds the maximum length",
                Map.of("maxLength", String.valueOf(maxLength)));
    }

    public static AppException invalidTimezone(String timezone) {
        return badRequest(NotificationErrorCode.NOTIFICATION_INVALID_TIMEZONE,
                "Notification timezone is invalid",
                Map.of("timezone", timezone));
    }

    private static AppException notFound(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.NOT_FOUND, details);
    }

    private static AppException conflict(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.CONFLICT, details);
    }

    private static AppException badRequest(String code, String message) {
        return new AppException(code, message, HttpStatus.BAD_REQUEST);
    }

    private static AppException badRequest(String code, String message, Map<String, String> details) {
        return new AppException(code, message, HttpStatus.BAD_REQUEST, details);
    }
}
