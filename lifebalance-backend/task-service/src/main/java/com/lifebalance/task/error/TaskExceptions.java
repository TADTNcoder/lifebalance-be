package com.lifebalance.task.error;

import com.lifebalance.common.error.AppException;
import org.springframework.http.HttpStatus;

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

    private static AppException notFound(String code, String message) {
        return new AppException(code, message, HttpStatus.NOT_FOUND);
    }

    private static AppException conflict(String code, String message) {
        return new AppException(code, message, HttpStatus.CONFLICT);
    }
}
