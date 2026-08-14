package com.lifebalance.task.exception;

public class InvalidTaskDeadlineException extends RuntimeException {

    public InvalidTaskDeadlineException(String message) {
        super(message);
    }
}