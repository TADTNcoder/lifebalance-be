package com.lifebalance.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.UserRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping({"/users", "/api/users"})
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)

public class UserRoleController {
    private final UserRoleService userRoleService;
    private final KeycloakUserMappingService keycloakUserMappingService;

    @Operation(summary = "Assign role to user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "404", description = "User or Role not found")
    })
    @PostMapping("/{userId}/roles")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:assign')")
    public void assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        CurrentUser currentUser = keycloakUserMappingService.map(jwt);

        userRoleService.assignRole(
                userId,
                request,
                UUID.fromString(currentUser.getUserId()));
    }

    @Operation(summary = "Remove role from user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role removed successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:assign')")
    public void removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        userRoleService.removeRole(userId, roleId);
    }

    @Operation(summary = "Get roles of user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @GetMapping("/{userId}/roles")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'role:read') || @permissionEvaluationService.isCurrentUser(authentication, #userId)")
    public List<RoleResponse> getRoles(
            @PathVariable UUID userId) {

        return userRoleService.getRoles(userId);
    }
}
