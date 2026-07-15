package com.lifebalance.identity.service;

import org.springframework.security.oauth2.jwt.Jwt;

import com.lifebalance.identity.security.CurrentUser;

public interface KeycloakUserMappingService {

    CurrentUser map(Jwt jwt);

}