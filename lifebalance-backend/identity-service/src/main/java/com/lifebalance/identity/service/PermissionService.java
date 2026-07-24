package com.lifebalance.identity.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;

public interface PermissionService {
    PermissionResponse create(CreatePermissionRequest request);

    List<PermissionResponse> getAll();

    PermissionResponse getById(UUID id);

    PermissionResponse update(UUID id, UpdatePermissionRequest request);

    void delete(UUID id);
}
