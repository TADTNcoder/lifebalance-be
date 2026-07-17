package com.lifebalance.identity.service;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.security.CurrentUser;

public interface InternalUserService {
    User findOrCreate(CurrentUser currentUser);

    User getCurrentUser(CurrentUser currentUser);
}
