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

    private TaskErrorCode() {
    }
}
