package com.lifebalance.identity.controller;

import java.util.UUID;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;

import org.springframework.http.ResponseEntity;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        private final InternalUserService internalUserService;
        private final KeycloakUserMappingService keycloakUserMappingService;
        private final UserService userService;
        private final AuditLogService auditLogService;

        @Operation(summary = "Get user by id", description = "Returns detail information for the requested user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Success"),
                        @ApiResponse(responseCode = "400", description = "Invalid user id"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/{id}")
        public UserResponse getUserById(
                        @Parameter(description = "User id in UUID format", required = true) @PathVariable UUID id) {
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
                        @Parameter(description = "User id in UUID format", required = true) @PathVariable UUID id,
                        @Valid @RequestBody UpdateUserRequest request) {
                return userService.updateUser(id, request);
        }

        @Operation(summary = "Activate user by id", description = "Activates an inactive or disabled user account")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Activated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid user id"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "User already active, deleted, or cannot be activated from the current status")
        })
        @PatchMapping("/{id}/activate")
        public UserResponse activateUser(
                        @Parameter(description = "User id in UUID format", required = true) @PathVariable UUID id) {
                return userService.activateUser(id);
        }

        @Operation(summary = "Disable user by id", description = "Disables a user account without soft deleting the record")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Disabled successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid user id"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "User already disabled or deleted")
        })
        @PatchMapping("/{id}/disable")
        public UserResponse disableUser(
                        @Parameter(description = "User id in UUID format", required = true) @PathVariable UUID id) {
                return userService.disableUser(id);
        }

        @Operation(summary = "Soft delete user by id", description = "Marks a user as deleted and excludes it from normal user queries")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid user id"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "User already deleted")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> softDeleteUser(
                        @Parameter(description = "User id in UUID format", required = true) @PathVariable UUID id) {
                userService.softDeleteUser(id);
                return ResponseEntity.noContent().build();
        }

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
        public UserResponse updateCurrentUser(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request,
                        @Valid @RequestBody UpdateUserRequest requestBody) {
                CurrentUser currentUser = keycloakUserMappingService.map(jwt);
                User user = internalUserService.updateCurrentUser(currentUser, requestBody);

                UserResponse response = new UserResponse();
                response.setId(user.getId());
                response.setEmail(user.getEmail());
                response.setUsername(user.getUsername());
                response.setDisplayName(user.getDisplayName());

                return toResponse(user);
        }

        private static UserResponse toResponse(User user) {
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

        @Operation(summary = "Search users with pagination")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Success")
        })

        @GetMapping
        public Page<UserResponse> searchUsers(

                        @RequestParam(defaultValue = "") String keyword,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return internalUserService.search(keyword, pageable);
        }
}

}
