package com.lifebalance.identity.service;

import com.lifebalance.identity.model.User;

public interface UserSessionRevocationService {

    void revokeSessions(User user, String reason);
}
