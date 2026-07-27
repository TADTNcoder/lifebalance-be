package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles")
    @GetMapping
    public List<RoleResponse> getAll() {
        return roleService.getAllRoles();
    }

    @Operation(summary = "Create role")
    @PostMapping
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(request);
    }

    @Operation(summary = "Get role by id")
    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable UUID id) {
        return roleService.getRoleById(id);
    }

    @Operation(summary = "Get role by code")
    @GetMapping("/code/{code}")
    public RoleResponse getByCode(@PathVariable String code) {
        return roleService.getRoleByCode(code);
    }

    @Operation(summary = "Update role")
    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @Operation(summary = "Assign permissions to role")
    @PutMapping("/{id}/permissions")
    public RoleResponse assignPermissions(@PathVariable UUID id, @RequestBody List<UUID> permissionIds) {
        return roleService.assignPermissionsToRole(id, permissionIds);
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        roleService.deleteRole(id);
    }
}
