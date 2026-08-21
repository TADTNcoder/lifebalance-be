package com.lifebalance.timeline.error;

import com.lifebalance.common.error.AppException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public final class TimelineExceptions {

    private TimelineExceptions() {
    }

    public static AppException taskNotFound(UUID taskId) {
        return notFound(TimelineErrorCode.TIMELINE_TASK_NOT_FOUND, "Timeline task snapshot not found",
                Map.of("taskId", String.valueOf(taskId)));
    }

    public static AppException invalidTaskSnapshot(String reason) {
        return badRequest(TimelineErrorCode.TIMELINE_TASK_SNAPSHOT_INVALID, "Timeline task snapshot is invalid",
                Map.of("reason", reason));
    }

    public static AppException taskNotEligible(UUID taskId, String reason) {
        return conflict(TimelineErrorCode.TIMELINE_TASK_NOT_ELIGIBLE, "Task is not eligible for timeline placement",
                Map.of("taskId", String.valueOf(taskId), "reason", reason));
    }

    public static AppException placementNotFound(UUID placementId) {
        return notFound(TimelineErrorCode.TIMELINE_PLACEMENT_NOT_FOUND, "Timeline placement not found",
                Map.of("placementId", String.valueOf(placementId)));
    }

    public static AppException placementNotActive(UUID placementId) {
        return conflict(TimelineErrorCode.TIMELINE_PLACEMENT_NOT_ACTIVE, "Timeline placement is not active",
                Map.of("placementId", String.valueOf(placementId)));
    }

    public static AppException invalidWindow() {
        return badRequest(TimelineErrorCode.TIMELINE_INVALID_WINDOW,
                "Timeline placement start time must be before end time");
    }

    public static AppException invalidCycle(String reason) {
        return badRequest(TimelineErrorCode.TIMELINE_INVALID_CYCLE, "Timeline cycle window is invalid",
                Map.of("reason", reason));
    }

    public static AppException cycleViolation(UUID taskId, OffsetDateTime startAt, OffsetDateTime endAt) {
        return conflict(TimelineErrorCode.TIMELINE_INVALID_CYCLE,
                "Timeline placement is outside the allowed cycle window",
                Map.of(
                        "taskId", String.valueOf(taskId),
                        "requestedStartAt", String.valueOf(startAt),
                        "requestedEndAt", String.valueOf(endAt)
                ));
    }

    public static AppException deadlineViolation(UUID taskId, OffsetDateTime startAt) {
        return conflict(TimelineErrorCode.TIMELINE_DEADLINE_VIOLATION,
                "Timeline placement violates task deadline",
                Map.of(
                        "taskId", String.valueOf(taskId),
                        "requestedStartAt", String.valueOf(startAt)
                ));
    }

    public static AppException conflict(UUID ownerId, int conflictCount) {
        return conflict(TimelineErrorCode.TIMELINE_CONFLICT,
                "Timeline placement conflicts with existing active placements",
                Map.of(
                        "ownerId", String.valueOf(ownerId),
                        "conflictCount", String.valueOf(conflictCount)
                ));
    }

    public static AppException conflictConfirmationRequired(UUID ownerId, int conflictCount) {
        return new AppException(
                TimelineErrorCode.TIMELINE_CONFLICT_CONFIRMATION_REQUIRED,
                "Timeline conflict requires explicit confirmation",
                HttpStatus.CONFLICT,
                Map.of(
                        "ownerId", String.valueOf(ownerId),
                        "conflictCount", String.valueOf(conflictCount),
                        "confirmationField", "conflictConfirmed",
                        "confirmationRequired", "true"
                ));
    }

    public static AppException invalidTextLength(int maxLength) {
        return badRequest(TimelineErrorCode.TIMELINE_TEXT_TOO_LONG, "Timeline text is too long",
                Map.of("maxLength", String.valueOf(maxLength)));
    }

    public static AppException invalidTimezone(String timezone) {
        return badRequest(TimelineErrorCode.TIMELINE_INVALID_TIMEZONE, "Timeline timezone is invalid",
                Map.of("timezone", String.valueOf(timezone)));
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
