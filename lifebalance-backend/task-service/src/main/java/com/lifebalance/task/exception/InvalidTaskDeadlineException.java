package com.lifebalance.task.exception;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.error.TaskErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidTaskDeadlineException extends AppException {

    public InvalidTaskDeadlineException(String message) {
        super(TaskErrorCode.TASK_INVALID_DEADLINE, message, HttpStatus.BAD_REQUEST);
    }
}
