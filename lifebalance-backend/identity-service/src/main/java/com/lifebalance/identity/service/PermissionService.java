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
}
