package com.lifebalance.identity.service;

import com.lifebalance.identity.model.User;

public interface UserSessionRevocationService {

    /**
     * Blocks new authentication for the user and revokes all existing sessions.
     */
    void revokeSessions(User user, String reason);

    /**
     * Restores authentication after an account is activated or unlocked.
     */
    void restoreAccess(User user, String reason);
}
