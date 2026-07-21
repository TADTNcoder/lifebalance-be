package com.lifebalance.identity.controller;

import java.util.UUID;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final InternalUserService internalUserService;
    private final KeycloakUserMappingService keycloakUserMappingService;
    private final UserService userService;

    @Operation(summary = "Get user by id", description = "Returns detail information for the requested user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid user id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Partially update user by id", description = "Updates the provided user fields and leaves omitted fields unchanged")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user id or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Get current user profile", description = "Returns the profile information of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        User user = internalUserService.getCurrentUser(currentUser);
        auditLogService.saveAudit(
                user,
                AuditAction.LOGIN,
                AuditStatus.SUCCESS,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                "User login successfully");

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setStatus(user.getStatus());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;

    }

    @Operation(summary = "Update current user profile", description = "Updates the profile information of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me")
    public UserResponse updateCurrentUser(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest requestBody) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        User user = internalUserService.updateCurrentUser(currentUser, requestBody);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setStatus(user.getStatus());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
    }
}
