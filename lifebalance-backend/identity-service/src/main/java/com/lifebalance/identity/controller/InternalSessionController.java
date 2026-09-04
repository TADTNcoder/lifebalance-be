package com.lifebalance.identity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/session")
@RequiredArgsConstructor
public class InternalSessionController {

    private final InternalUserService internalUserService;
    private final KeycloakUserMappingService keycloakUserMappingService;

    @GetMapping("/validate")
    public ResponseEntity<Void> validateSession(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        internalUserService.validateSession(currentUser, jwt.getIssuedAt());
        return ResponseEntity.noContent().build();
    }
}
