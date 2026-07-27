package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Permissions", description = "Permission Management APIs")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Get all permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @GetMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:read')")
    public List<PermissionResponse> getAll() {
        return permissionService.getAll();
    }

    @Operation(summary = "Get permission by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
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
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:create')")
    public PermissionResponse create(
            @Valid @RequestBody CreatePermissionRequest request) {

        return permissionService.create(request);
    }

    @Operation(summary = "Update permission")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:update')")
    public PermissionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {

        return permissionService.update(id, request);
    }

    @Operation(summary = "Delete permission")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'permission:delete')")
    public void delete(@PathVariable UUID id) {

        permissionService.delete(id);
    }

}
