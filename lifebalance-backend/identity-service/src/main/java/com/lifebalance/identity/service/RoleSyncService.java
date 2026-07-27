package com.lifebalance.identity.service;

import java.util.Collection;

import com.lifebalance.identity.model.Role;

public interface RoleSyncService {

    void syncCreatedRole(Role role);

    void syncUpdatedRole(Role role);

    void syncDeletedRole(Role role);

    int syncAllRoles(Collection<Role> roles);
}
