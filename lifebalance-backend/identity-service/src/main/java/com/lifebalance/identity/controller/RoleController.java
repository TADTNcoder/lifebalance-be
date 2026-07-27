package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Roles", description = "Role Management APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/roles", "/api/roles"})
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
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
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PostMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:create')")
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(request);
    }

    @Operation(summary = "Get role by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:read')")
    public RoleResponse getById(
            @Parameter(description = "Role id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        return roleService.getRoleById(id);
    }

    @Operation(summary = "Get role by code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/code/{code}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:read')")
    public RoleResponse getByCode(
            @Parameter(description = "Role code", required = true, example = "MANAGER")
            @PathVariable String code
    ) {
        return roleService.getRoleByCode(code);
    }

    @Operation(summary = "Update role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:update')")
    public RoleResponse update(
            @Parameter(description = "Role id in UUID format", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return roleService.updateRole(id, request);
    }

    @Operation(summary = "Assign permissions to role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{id}/permissions")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:assign')")
    public RoleResponse assignPermissions(
            @Parameter(description = "Role id in UUID format", required = true)
            @PathVariable UUID id,
            @RequestBody List<UUID> permissionIds
    ) {
        return roleService.assignPermissionsToRole(id, permissionIds);
    }

    @Operation(summary = "Delete role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:delete')")
    public void delete(
            @Parameter(description = "Role id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        roleService.deleteRole(id);
    }
}
