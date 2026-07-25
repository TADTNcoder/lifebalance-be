package com.lifebalance.identity.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.repository.PermissionRepository;
import com.lifebalance.identity.service.PermissionService;

import jakarta.transaction.Transactional;
import jakarta.validation.OverridesAttribute;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional
    @Override
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Permission code already exists");
        }
        Permission permission = Permission.builder()
                .code(request.getCode())
                .name(request.getName())
                .module(request.getModule())
                .description(request.getDescription())
                .build();

        permission = permissionRepository.save(permission);

        return mapToResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PermissionResponse getById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        return mapToResponse(permission);
    }

    @Transactional
    @Override
    public PermissionResponse update(UUID id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setName(request.getName());
        permission.setModule(request.getModule());
        permission.setDescription(request.getDescription());
        permission = permissionRepository.save(permission);
        return mapToResponse(permission);
    }

    @Transactional
    @Override
    public void delete(UUID id) {

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        permissionRepository.delete(permission);
    }

    private PermissionResponse mapToResponse(Permission permission) {

        PermissionResponse response = new PermissionResponse();

        response.setId(permission.getId());
        response.setCode(permission.getCode());
        response.setName(permission.getName());
        response.setModule(permission.getModule());
        response.setDescription(permission.getDescription());
        response.setCreatedAt(permission.getCreatedAt());
        response.setUpdatedAt(permission.getUpdatedAt());

        return response;
    }
}
