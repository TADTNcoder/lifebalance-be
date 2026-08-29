package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.error.IdentityErrorCode;

class InMemoryPasswordChangeAttemptLimiterTest {

    private MutableClock clock;
    private InMemoryPasswordChangeAttemptLimiter limiter;

    @BeforeEach
    void setUp() {
        PasswordChangeProperties properties = new PasswordChangeProperties();
        properties.setMaximumAttempts(2);
        properties.setAttemptWindow(Duration.ofMinutes(10));
        clock = new MutableClock(Instant.parse("2026-08-29T03:00:00Z"));
        limiter = new InMemoryPasswordChangeAttemptLimiter(properties, clock);
    }

    @Test
    void shouldBlockAfterConfiguredNumberOfFailures() {
        limiter.recordFailure("user|ip");
        limiter.recordFailure("user|ip");

        assertThatThrownBy(() -> limiter.checkAllowed("user|ip"))
                .isInstanceOfSatisfying(AppException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(IdentityErrorCode.PASSWORD_CHANGE_RATE_LIMITED);
                    assertThat(exception.getDetails()).containsEntry("retryAfterSeconds", "600");
                });
    }

    @Test
    void shouldAllowAttemptsAgainAfterWindowOrExplicitReset() {
        limiter.recordFailure("user|ip");
        limiter.recordFailure("user|ip");
        clock.advance(Duration.ofMinutes(10));
        assertThatCode(() -> limiter.checkAllowed("user|ip")).doesNotThrowAnyException();

        limiter.recordFailure("user|ip");
        limiter.recordFailure("user|ip");
        limiter.reset("user|ip");
        assertThatCode(() -> limiter.checkAllowed("user|ip")).doesNotThrowAnyException();
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
