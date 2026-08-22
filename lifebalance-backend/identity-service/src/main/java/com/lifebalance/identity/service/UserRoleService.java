package com.lifebalance.identity.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;

public interface UserRoleService {

    void assignRole(
            UUID userId,
            AssignRoleRequest request,
            String assignedByKeycloakId);

    void removeRole(
            UUID userId,
            UUID roleId);

    List<RoleResponse> getRoles(
            UUID userId);
}
