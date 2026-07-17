package com.lifebalance.identity.service.impl;

import org.springframework.security.oauth2.jwt.Jwt;

import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.KeycloakUserMappingService;

public class KeycloakUserMappingServiceImpl implements KeycloakUserMappingService {

    @Override
    public CurrentUser map(Jwt jwt) {

        CurrentUser user = new CurrentUser();

        user.setUserId(jwt.getSubject());

        user.setUsername(jwt.getClaimAsString("preferred_username"));

        user.setEmail(jwt.getClaimAsString("email"));

        user.setRoles(jwt.getClaimAsStringList("roles"));
        return user;
    }
}
