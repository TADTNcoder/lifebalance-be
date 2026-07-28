package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.service.PermissionService;
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

@Tag(name = "Permissions", description = "Permission Management APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/permissions", "/api/permissions"})
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Get all permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:read')")
    public List<PermissionResponse> getAll() {
        return permissionService.getAll();
    }

    @Operation(summary = "Get permission by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:read')")
    public PermissionResponse getById(@PathVariable UUID id) {
        return permissionService.getById(id);
    }

    @Operation(summary = "Create permission")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:create')")
    public PermissionResponse create(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        return permissionService.create(request);
    }

    @Operation(summary = "Update permission")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:update')")
    public PermissionResponse update(
            @Parameter(description = "Permission id in UUID format", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return permissionService.update(id, request);
    }

    @Operation(summary = "Delete permission")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:delete')")
    public void delete(
            @Parameter(description = "Permission id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        permissionService.delete(id);
    }
}