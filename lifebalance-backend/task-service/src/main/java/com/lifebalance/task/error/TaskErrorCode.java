package com.lifebalance.task.error;

public final class TaskErrorCode {

    public static final String TASK_NOT_FOUND = "TASK_NOT_FOUND";
    public static final String CATEGORY_NOT_FOUND = "TASK_CATEGORY_NOT_FOUND";
    public static final String TAG_NOT_FOUND = "TASK_TAG_NOT_FOUND";
    public static final String TASK_NAME_ALREADY_EXISTS = "TASK_NAME_ALREADY_EXISTS";
    public static final String CATEGORY_NAME_ALREADY_EXISTS = "TASK_CATEGORY_NAME_ALREADY_EXISTS";
    public static final String CATEGORY_SLUG_ALREADY_EXISTS = "TASK_CATEGORY_SLUG_ALREADY_EXISTS";
    public static final String TAG_NAME_ALREADY_EXISTS = "TASK_TAG_NAME_ALREADY_EXISTS";
    public static final String TASK_TAG_ALREADY_ASSIGNED = "TASK_TAG_ALREADY_ASSIGNED";
    public static final String TASK_TAG_NOT_ASSIGNED = "TASK_TAG_NOT_ASSIGNED";
    public static final String TASK_INVALID_DEADLINE = "TASK_INVALID_DEADLINE";
    public static final String TASK_INVALID_STATUS_TRANSITION = "TASK_INVALID_STATUS_TRANSITION";
    public static final String TASK_PLANNING_LOCKED = "TASK_PLANNING_LOCKED";
    public static final String TASK_TIMELINE_NOT_ELIGIBLE = "TASK_TIMELINE_NOT_ELIGIBLE";
    public static final String TASK_TIMELINE_INVALID_WINDOW = "TASK_TIMELINE_INVALID_WINDOW";
    public static final String TASK_TIMELINE_CONFLICT = "TASK_TIMELINE_CONFLICT";
    public static final String TASK_TIMELINE_PLACEMENT_NOT_FOUND = "TASK_TIMELINE_PLACEMENT_NOT_FOUND";
    public static final String TASK_DELETE_NOT_ALLOWED = "TASK_DELETE_NOT_ALLOWED";
    public static final String TASK_PROGRESS_NOT_ALLOWED = "TASK_PROGRESS_NOT_ALLOWED";
    public static final String TASK_OPTIONAL_FEATURE_NOT_APPROVED = "TASK_OPTIONAL_FEATURE_NOT_APPROVED";
    public static final String TASK_RECURRING_RULE_NOT_FOUND = "TASK_RECURRING_RULE_NOT_FOUND";
    public static final String TASK_RECURRING_RULE_INVALID = "TASK_RECURRING_RULE_INVALID";
    public static final String TASK_REMINDER_NOT_FOUND = "TASK_REMINDER_NOT_FOUND";
    public static final String TASK_REMINDER_INVALID = "TASK_REMINDER_INVALID";

    private TaskErrorCode() {
    }
}
