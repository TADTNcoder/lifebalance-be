package com.lifebalance.identity.controller;

import java.util.UUID;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.dto.LockUserRequest;
import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User Management APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/users", "/api/users"})
@RequiredArgsConstructor
public class UserController {

    private final InternalUserService internalUserService;
    private final KeycloakUserMappingService keycloakUserMappingService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    @Operation(summary = "Get current user profile", description = "Returns the profile information of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        User user = internalUserService.findOrCreate(currentUser);
        auditLogService.saveAudit(
                user,
                AuditAction.LOGIN,
                AuditStatus.SUCCESS,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                "User login successfully");

        return toResponse(user);
    }

    @Operation(summary = "Update current user profile", description = "Updates the profile information of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me")
    public UserResponse updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest requestBody
    ) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        User user = internalUserService.updateCurrentUser(currentUser, requestBody);
        auditLogService.saveAudit(
                user,
                AuditAction.UPDATE_USER,
                AuditStatus.SUCCESS,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                "User updated profile");
        return toResponse(user);
    }

    @Operation(summary = "Get user by id", description = "Returns detail information for the requested user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid user id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'User', 'READ')")
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
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'User', 'UPDATE')")
    public UserResponse updateUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Activate user by id", description = "Activates an inactive or disabled user account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already active, deleted, or cannot be activated from the current status")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:update')")
    public UserResponse activateUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        return userService.activateUser(id);
    }

    @Operation(summary = "Disable user by id", description = "Disables a user account without soft deleting the record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disabled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already disabled or deleted")
    })
    @PatchMapping("/{id}/disable")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:update')")
    public UserResponse disableUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        return userService.disableUser(id);
    }

    @Operation(summary = "Lock user by id", description = "Locks a user account and revokes existing user sessions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user id or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already locked, deleted, or self-lock is not allowed")
    })
    @PatchMapping("/{id}/lock")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:update')")
    public UserResponse lockUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LockUserRequest request
    ) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);

        return userService.lockUser(id, currentUser.getUserId(), request);
    }

    @Operation(summary = "Unlock user by id", description = "Unlocks a locked user account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unlocked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User is not locked or was already deleted")
    })
    @PatchMapping("/{id}/unlock")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:update')")
    public UserResponse unlockUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        return userService.unlockUser(id);
    }

    @Operation(summary = "Soft delete user by id", description = "Marks a user as deleted and excludes it from normal user queries")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already deleted")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:delete')")
    public ResponseEntity<Void> softDeleteUser(
            @Parameter(description = "User id in UUID format", required = true)
            @PathVariable UUID id
    ) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search users with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'user:read')")
    public Page<UserResponse> searchUsers(
            @Parameter(description = "Case-insensitive search keyword applied by the service", example = "alice")
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageableLimits.of(page, size);

        return internalUserService.search(keyword, pageable);
    }

    private static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setBirthDate(user.getBirthDate());
        response.setStatus(user.getStatus());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setLockReason(user.getLockReason());
        response.setLockedAt(user.getLockedAt());
        response.setLockedUntil(user.getLockedUntil());

        return response;
    }
}
