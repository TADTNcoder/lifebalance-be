package com.lifebalance.identity.service;

import com.lifebalance.identity.dto.CheckPermissionResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.security.CurrentUser;

public interface AuthorizationService {

    CheckPermissionResponse checkPermission(
            User user,
            CurrentUser currentUser,
            String permissionCode
    );
}
