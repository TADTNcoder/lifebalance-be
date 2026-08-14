package com.lifebalance.identity.controller;

import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.service.RolePermissionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping({"/roles", "/api/roles"})
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:read')")
    public List<PermissionResponse> getPermissions(
            @PathVariable UUID roleId) {

        return rolePermissionService.getPermissions(roleId);
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:assign')")
    public void assignPermission(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionRequest request) {

        rolePermissionService.assignPermission(
                roleId,
                request);
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:assign')")
    public void removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        rolePermissionService.removePermission(
                roleId,
                permissionId);
    }
}
