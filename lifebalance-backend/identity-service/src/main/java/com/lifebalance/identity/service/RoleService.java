package com.lifebalance.identity.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(UUID id);

    RoleResponse getRoleByCode(String code);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(UUID id, UpdateRoleRequest request);

    void deleteRole(UUID id);

    RoleResponse assignPermissionsToRole(UUID roleId, Collection<UUID> permissionIds);

    default RoleResponse create(CreateRoleRequest request) {
        return createRole(request);
    }

    default List<RoleResponse> getAll() {
        return getAllRoles();
    }

    default RoleResponse getById(UUID id) {
        return getRoleById(id);
    }

    default RoleResponse update(UUID id, UpdateRoleRequest request) {
        return updateRole(id, request);
    }

    default void delete(UUID id) {
        deleteRole(id);
    }
}
