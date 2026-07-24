package com.lifebalance.identity.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.RoleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    @Override
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Role code already exists");
        }

        Role role = Role.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .system(request.getSystem())
                .build();
        role = roleRepository.save(role);

        return mapToResponse(role);
    }

    @Override
    public List<RoleResponse> getAll() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoleResponse getById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        return mapToResponse(role);
    }

    @Transactional
    @Override
    public RoleResponse update(UUID id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        if (request.getSystem() != null) {
            role.setSystem(request.getSystem());
        }
        role = roleRepository.save(role);

        return mapToResponse(role);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    private RoleResponse mapToResponse(Role role) {

        RoleResponse response = new RoleResponse();

        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setSystem(role.getSystem());
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());

        return response;
    }

}
