package com.lifebalance.identity.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.error.PasswordChangeExceptions;
import com.lifebalance.identity.service.PasswordChangeAttemptLimiter;

public class InMemoryPasswordChangeAttemptLimiter implements PasswordChangeAttemptLimiter {

    private static final long MAXIMUM_TRACKED_SUBJECTS = 50_000L;

    private final int maximumAttempts;
    private final Duration attemptWindow;
    private final Clock clock;
    private final Cache<String, AttemptState> attempts;

    public InMemoryPasswordChangeAttemptLimiter(PasswordChangeProperties properties, Clock clock) {
        this.maximumAttempts = properties.getMaximumAttempts();
        this.attemptWindow = properties.getAttemptWindow();
        this.clock = clock;
        this.attempts = Caffeine.newBuilder()
                .maximumSize(MAXIMUM_TRACKED_SUBJECTS)
                .expireAfterWrite(attemptWindow.multipliedBy(2L))
                .build();
    }

    @Override
    public synchronized void checkAllowed(String subjectKey) {
        Instant now = clock.instant();
        AttemptState state = activeState(subjectKey, now);
        if (state == null || state.failures() < maximumAttempts) {
            return;
        }

        long retryAfterSeconds = Math.max(
                1L,
                Duration.between(now, state.firstFailureAt().plus(attemptWindow)).toSeconds()
        );
        throw PasswordChangeExceptions.rateLimited(retryAfterSeconds);
    }

    @Override
    public synchronized void recordFailure(String subjectKey) {
        Instant now = clock.instant();
        AttemptState state = activeState(subjectKey, now);
        if (state == null) {
            attempts.put(subjectKey, new AttemptState(1, now));
            return;
        }

        attempts.put(subjectKey, new AttemptState(state.failures() + 1, state.firstFailureAt()));
    }

    @Override
    public synchronized void reset(String subjectKey) {
        attempts.invalidate(subjectKey);
    }

    private AttemptState activeState(String subjectKey, Instant now) {
        AttemptState state = attempts.getIfPresent(subjectKey);
        if (state == null) {
            return null;
        }

        if (!now.isBefore(state.firstFailureAt().plus(attemptWindow))) {
            attempts.invalidate(subjectKey);
            return null;
        }
        return state;
    }

    private record AttemptState(int failures, Instant firstFailureAt) {
    }
}
