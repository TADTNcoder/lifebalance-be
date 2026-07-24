package com.lifebalance.identity.service;

import java.util.List;
import java.util.UUID;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    List<RoleResponse> getAll();

    RoleResponse getById(UUID id);

    RoleResponse update(UUID id, UpdateRoleRequest request);

    void delete(UUID id);
}
