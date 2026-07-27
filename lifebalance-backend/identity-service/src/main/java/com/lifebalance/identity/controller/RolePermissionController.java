package com.lifebalance.identity.controller;

import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.service.RolePermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/{roleId}/permissions")
    public List<PermissionResponse> getPermissions(
            @PathVariable UUID roleId) {

        return rolePermissionService.getPermissions(roleId);
    }

    @PostMapping("/{roleId}/permissions")
    public void assignPermission(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionRequest request) {

        rolePermissionService.assignPermission(
                roleId,
                request);
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public void removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        rolePermissionService.removePermission(
                roleId,
                permissionId);
    }
}
