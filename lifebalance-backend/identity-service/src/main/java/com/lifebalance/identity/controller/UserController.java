package com.lifebalance.identity.controller;

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

import io.swagger.v3.oas.annotations.Operation;
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
    private final AuditLogService auditLogService;

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

        return response;
    }
}
