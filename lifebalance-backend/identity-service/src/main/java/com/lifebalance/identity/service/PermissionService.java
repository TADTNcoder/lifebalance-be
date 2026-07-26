package com.lifebalance.identity.service;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PermissionService {

    List<PermissionResponse> getAllPermissions();

    List<PermissionResponse> getPermissionsByModule(String module);

    PermissionResponse getPermissionById(UUID id);

    PermissionResponse getPermissionByCode(String code);

    PermissionResponse createPermission(CreatePermissionRequest request);

    PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request);

    void deletePermission(UUID id);

    List<PermissionResponse> getPermissionsByRoleIds(Collection<UUID> roleIds);

    default List<PermissionResponse> getAll() {
        return getAllPermissions();
    }

    default PermissionResponse getById(UUID id) {
        return getPermissionById(id);
    }

    default PermissionResponse create(CreatePermissionRequest request) {
        return createPermission(request);
    }

    default PermissionResponse update(UUID id, UpdatePermissionRequest request) {
        return updatePermission(id, request);
    }

    default void delete(UUID id) {
        deletePermission(id);
    }
}
