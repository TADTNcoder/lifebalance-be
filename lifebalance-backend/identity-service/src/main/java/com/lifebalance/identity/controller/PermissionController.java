package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public List<PermissionResponse> getAll() {
        return permissionService.getAll();
    }

    @Operation(summary = "Get permission by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    @GetMapping("/{id}")
    public PermissionResponse getById(@PathVariable UUID id) {
        return permissionService.getById(id);
    }

    @Operation(summary = "Create permission")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
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
    public void delete(@PathVariable UUID id) {

        permissionService.delete(id);
    }

}
