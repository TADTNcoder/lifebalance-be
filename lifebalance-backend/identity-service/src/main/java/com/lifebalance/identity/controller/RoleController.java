package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.RoleSyncResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Roles", description = "Role Management APIs")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:read')")
    public List<RoleResponse> getAll() {
        return roleService.getAllRoles();
    }

    @Operation(summary = "Create role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PostMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:create')")
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(request);
    }

    @Operation(summary = "Synchronize all roles to Keycloak")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Synchronized successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/sync/keycloak")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:update')")
    public RoleSyncResponse syncAllToKeycloak() {
        return roleService.syncAllRolesToKeycloak();
    }

    @Operation(summary = "Get role by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:read')")
    public RoleResponse getById(@PathVariable UUID id) {
        return roleService.getRoleById(id);
    }

    @Operation(summary = "Get role by code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/code/{code}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:read')")
    public RoleResponse getByCode(@PathVariable String code) {
        return roleService.getRoleByCode(code);
    }

    @Operation(summary = "Update role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:update')")
    public RoleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @Operation(summary = "Assign permissions to role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{id}/permissions")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:assign')")
    public RoleResponse assignPermissions(@PathVariable UUID id, @RequestBody List<UUID> permissionIds) {
        return roleService.assignPermissionsToRole(id, permissionIds);
    }

    @Operation(summary = "Delete role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:delete')")
    public void delete(@PathVariable UUID id) {
        roleService.deleteRole(id);
    }
}
