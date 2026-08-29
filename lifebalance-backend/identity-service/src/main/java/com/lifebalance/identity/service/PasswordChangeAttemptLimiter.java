package com.lifebalance.identity.service;

public interface PasswordChangeAttemptLimiter {

    void checkAllowed(String subjectKey);

    void recordFailure(String subjectKey);

    void reset(String subjectKey);
}
