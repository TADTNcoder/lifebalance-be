package com.lifebalance.identity.service.impl;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.UserSessionRevocationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoopUserSessionRevocationService implements UserSessionRevocationService {

    @Override
    public void revokeSessions(User user, String reason) {
        log.debug(
                "Session revocation requested for user {} with reason '{}', but no revocation provider is configured",
                user.getId(),
                reason
        );
    }
}
