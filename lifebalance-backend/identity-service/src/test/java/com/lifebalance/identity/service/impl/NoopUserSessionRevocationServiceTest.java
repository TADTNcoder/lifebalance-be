package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

import com.lifebalance.identity.model.User;

class NoopUserSessionRevocationServiceTest {

    @Test
    void shouldExecuteWithoutErrors() {
        NoopUserSessionRevocationService service = new NoopUserSessionRevocationService();
        User user = new User();

        assertThatNoException().isThrownBy(() -> service.revokeSessions(user, "test-reason"));
        assertThatNoException().isThrownBy(() -> service.restoreAccess(user, "test-reason"));
    }
}
