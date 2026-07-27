package com.lifebalance.identity.service.impl;

import java.util.Collection;

import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.service.RoleSyncService;

public class NoopRoleSyncService implements RoleSyncService {

    @Override
    public void syncCreatedRole(Role role) {
    }

    @Override
    public void syncUpdatedRole(Role role) {
    }

    @Override
    public void syncDeletedRole(Role role) {
    }

    @Override
    public int syncAllRoles(Collection<Role> roles) {
        return roles == null ? 0 : roles.size();
    }
}
