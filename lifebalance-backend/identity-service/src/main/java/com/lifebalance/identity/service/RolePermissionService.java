package com.lifebalance.identity.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;

public interface RolePermissionService {

    void assignPermission(
            UUID roleId,
            AssignPermissionRequest request);

    void removePermission(
            UUID roleId,
            UUID permissionId);

    List<PermissionResponse> getPermissions(UUID roleId);
}