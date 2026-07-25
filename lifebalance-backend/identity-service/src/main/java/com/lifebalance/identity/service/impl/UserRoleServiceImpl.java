package com.lifebalance.identity.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.repository.UserRoleRepository;
import com.lifebalance.identity.service.UserRoleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    @Transactional
    @Override
    public void assignRole(
            UUID userId,
            AssignRoleRequest request,
            UUID assignedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (userRoleRepository.existsByUserIdAndRoleId(userId, request.getRoleId())) {
            throw new RuntimeException("Role already assigned");
        }
        User assigner = userRepository.findById(assignedBy)
                .orElseThrow(() -> new RuntimeException("AssignedBy not found"));
        UserRole userRole = new UserRole();

        userRole.setId(new UserRoleId(
                user.getId(),
                role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assigner);

        userRoleRepository.save(userRole);
    }

    @Transactional
    @Override
    public void removeRole(
            UUID userId,
            UUID roleId) {

        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new RuntimeException("User does not have this role");
        }

        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public List<RoleResponse> getRoles(UUID userId) {

        return userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRole)
                .map(this::mapToResponse)
                .toList();
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
