package com.lifebalance.task.error;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.model.enums.TaskStatus;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class TaskExceptions {

    private TaskExceptions() {
    }

    public static AppException taskNotFound() {
        return notFound(TaskErrorCode.TASK_NOT_FOUND, "Task not found");
    }

    public static AppException categoryNotFound() {
        return notFound(TaskErrorCode.CATEGORY_NOT_FOUND, "Category not found");
    }

    public static AppException tagNotFound() {
        return notFound(TaskErrorCode.TAG_NOT_FOUND, "Tag not found");
    }

    public static AppException taskNameAlreadyExists() {
        return conflict(TaskErrorCode.TASK_NAME_ALREADY_EXISTS, "Task name already exists");
    }

    public static AppException categoryNameAlreadyExists() {
        return conflict(TaskErrorCode.CATEGORY_NAME_ALREADY_EXISTS, "Category name already exists");
    }

    public static AppException categorySlugAlreadyExists() {
        return conflict(TaskErrorCode.CATEGORY_SLUG_ALREADY_EXISTS, "Category slug already exists");
    }

    public static AppException tagNameAlreadyExists() {
        return conflict(TaskErrorCode.TAG_NAME_ALREADY_EXISTS, "Tag name already exists");
    }

    public static AppException taskTagAlreadyAssigned() {
        return conflict(TaskErrorCode.TASK_TAG_ALREADY_ASSIGNED, "Tag already assigned to task");
    }

    public static AppException taskTagNotAssigned() {
        return conflict(TaskErrorCode.TASK_TAG_NOT_ASSIGNED, "Tag is not assigned to task");
    }

    public static AppException invalidStatusTransition(
            TaskStatus sourceStatus,
            TaskStatus targetStatus) {

        return conflict(
                TaskErrorCode.TASK_INVALID_STATUS_TRANSITION,
                "Task status transition is not allowed",
                Map.of(
                        "sourceStatus", String.valueOf(sourceStatus),
                        "targetStatus", String.valueOf(targetStatus)
                ));
    }

    public static AppException planningLocked(TaskStatus status) {
        return conflict(
                TaskErrorCode.TASK_PLANNING_LOCKED,
                "Task planning cannot be changed in its current status",
                Map.of("status", String.valueOf(status)));
    }

    public static AppException timelineNotEligible(String reason) {
        return conflict(
                TaskErrorCode.TASK_TIMELINE_NOT_ELIGIBLE,
                "Task is not eligible for timeline placement",
                Map.of("reason", reason));
    }

    public static AppException invalidTimelineWindow() {
        return badRequest(
                TaskErrorCode.TASK_TIMELINE_INVALID_WINDOW,
                "Timeline placement start time must be before end time");
    }

    public static AppException timelineConflict() {
        return conflict(
                TaskErrorCode.TASK_TIMELINE_CONFLICT,
                "Timeline placement conflicts with an existing active placement");
    }

    public static AppException timelinePlacementNotFound() {
        return notFound(
                TaskErrorCode.TASK_TIMELINE_PLACEMENT_NOT_FOUND,
                "Timeline placement not found");
    }

    public static AppException invalidSortCriteria(String sortBy, String sortDirection) {
        return badRequest(
                TaskErrorCode.TASK_INVALID_SORT_CRITERIA,
                "Task sort criteria is invalid",
                Map.of(
                        "sortBy", String.valueOf(sortBy),
                        "sortDirection", String.valueOf(sortDirection)
                ));
    }

    public static AppException deleteNotAllowed(TaskStatus status) {
        return conflict(
                TaskErrorCode.TASK_DELETE_NOT_ALLOWED,
                "Task cannot be deleted in its current status",
                Map.of("status", String.valueOf(status)));
    }

    public static AppException progressNotAllowed(TaskStatus status) {
        return conflict(
                TaskErrorCode.TASK_PROGRESS_NOT_ALLOWED,
                "Task progress cannot be updated in its current status",
                Map.of("status", String.valueOf(status)));
    }

    public static AppException optionalFeatureNotApproved(String featureName) {
        return conflict(
                TaskErrorCode.TASK_OPTIONAL_FEATURE_NOT_APPROVED,
                "Optional task feature is not approved by policy",
                Map.of("feature", featureName));
    }

    public static AppException recurringRuleNotFound() {
        return notFound(TaskErrorCode.TASK_RECURRING_RULE_NOT_FOUND, "Task recurring rule not found");
    }

    public static AppException recurringRuleInvalid(String reason) {
        return badRequest(
                TaskErrorCode.TASK_RECURRING_RULE_INVALID,
                "Task recurring rule is invalid",
                Map.of("reason", reason));
    }

    public static AppException reminderNotFound() {
        return notFound(TaskErrorCode.TASK_REMINDER_NOT_FOUND, "Task reminder not found");
    }

    public static AppException reminderInvalid(String reason) {
        return badRequest(
                TaskErrorCode.TASK_REMINDER_INVALID,
                "Task reminder is invalid",
                Map.of("reason", reason));
    }

    private static AppException notFound(String code, String message) {
        return new AppException(code, message, HttpStatus.NOT_FOUND);
    }

    private static AppException conflict(String code, String message) {
        return new AppException(code, message, HttpStatus.CONFLICT);
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
