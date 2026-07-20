package com.lifebalance.identity.service.impl;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.security.keycloak.KeycloakUserMapper;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeycloakUserMappingServiceImpl implements KeycloakUserMappingService {

    private final KeycloakUserMapper keycloakUserMapper;

    @Override
    public CurrentUser map(Jwt jwt) {
        KeycloakUserPrincipal principal = keycloakUserMapper.map(jwt);

        CurrentUser user = new CurrentUser();

        user.setUserId(principal.subject());

        user.setUsername(principal.username());

        user.setEmail(principal.email());

        user.setRoles(principal.roles().stream().sorted().toList());
        return user;
    }
}
