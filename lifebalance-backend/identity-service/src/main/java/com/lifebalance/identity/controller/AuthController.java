package com.lifebalance.identity.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.identity.dto.CheckPermissionResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuthorizationService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/auth", "/api/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakUserMappingService keycloakUserMappingService;
    private final InternalUserService internalUserService;
    private final AuthorizationService authorizationService;

    @Operation(
            summary = "Check current user permission",
            description = "Authenticates the bearer token and returns current user roles and permissions. When the permission query parameter is provided, the response includes whether that permission is granted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/check-permission")
    public CheckPermissionResponse checkPermission(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String permission
    ) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        User user = internalUserService.findOrCreate(currentUser);

        return authorizationService.checkPermission(
                user,
                currentUser,
                permission
        );
    }
}
