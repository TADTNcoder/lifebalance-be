package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.security.keycloak.KeycloakUserMapper;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;

@ExtendWith(MockitoExtension.class)
class KeycloakUserMappingServiceImplTest {

    @Mock
    private KeycloakUserMapper keycloakUserMapper;

    @InjectMocks
    private KeycloakUserMappingServiceImpl mappingService;

    @Test
    void shouldMapJwtToCurrentUserCorrectly() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "kc-123")
                .build();

        KeycloakUserPrincipal principal = mock(KeycloakUserPrincipal.class);
        when(principal.subject()).thenReturn("kc-123");
        when(principal.username()).thenReturn("alice");
        when(principal.email()).thenReturn("alice@example.com");
        when(principal.roles()).thenReturn(Set.of("alpha", "beta")); // Sửa thành Set.of ở đây

        when(keycloakUserMapper.map(jwt)).thenReturn(principal);

        CurrentUser currentUser = mappingService.map(jwt);

        assertThat(currentUser.getUserId()).isEqualTo("kc-123");
        assertThat(currentUser.getUsername()).isEqualTo("alice");
        assertThat(currentUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(currentUser.getRoles()).containsExactly("alpha", "beta");
    }
}