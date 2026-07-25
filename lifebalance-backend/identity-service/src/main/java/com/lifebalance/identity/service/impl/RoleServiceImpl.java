package com.lifebalance.identity.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.exception.RoleNotFoundException;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.service.RoleBusinessValidator;
import com.lifebalance.identity.service.RoleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleBusinessValidator roleBusinessValidator;

    @Transactional
    @Override
    public RoleResponse create(CreateRoleRequest request) {
        roleBusinessValidator.validateCreate(request);

        Role role = Role.builder()
                .code(normalizeCode(request.getCode()))
                .name(trimToNull(request.getName()))
                .description(trimToNull(request.getDescription()))
                .system(false)
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
                .orElseThrow(() -> new RoleNotFoundException(id));

        return mapToResponse(role);
    }

    @Transactional
    @Override
    public RoleResponse update(UUID id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        roleBusinessValidator.validateUpdate(role, request);

        role.setName(trimToNull(request.getName()));
        role.setDescription(trimToNull(request.getDescription()));
        role = roleRepository.save(role);

        return mapToResponse(role);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        roleBusinessValidator.validateDelete(role);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeCode(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

}
