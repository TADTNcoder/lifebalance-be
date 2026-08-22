package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.PermissionEvaluationContext;
import com.lifebalance.identity.security.PermissionMatcher;
import com.lifebalance.identity.service.RbacAuthorizationService;

@ExtendWith(MockitoExtension.class)
class PermissionEvaluationServiceImplTest {

    @Mock
    private RbacAuthorizationService rbacAuthorizationService;

    @Mock
    private UserRepository userRepository;

    private PermissionEvaluationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PermissionEvaluationServiceImpl(
                rbacAuthorizationService,
                userRepository,
                new PermissionMatcher()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTrueWhenUserHasExactPermission() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        stubAuthorization(
                "kc-user-1",
                userId,
                Set.of("user:read", "task:update")
        );

        assertThat(service.hasPermission(authentication, "user:read")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotHavePermission() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        stubAuthorization("kc-user-1", userId, Set.of("user:read"));

        assertThat(service.hasPermission(authentication, "user:delete")).isFalse();
    }

    @Test
    void shouldReturnTrueWhenUserHasWildcardPermission() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        stubAuthorization("kc-user-1", userId, Set.of("user:*"));

        assertThat(service.hasPermission(authentication, "user:delete")).isTrue();
        assertThat(service.hasPermission(authentication, "user", "update")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsMissingOrUnauthenticated() {
        assertThat(service.hasPermission(null, "user:read")).isFalse();

        TestingAuthenticationToken unauthenticated =
                new TestingAuthenticationToken("kc-user-1", "credentials");
        unauthenticated.setAuthenticated(false);

        assertThat(service.hasPermission(unauthenticated, "user:read")).isFalse();
        verifyNoInteractions(userRepository, rbacAuthorizationService);
    }

    @Test
    void shouldUseSecurityContextAuthenticationWhenAuthenticationIsNotProvided() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        stubAuthorization("kc-user-1", userId, Set.of("task:read"));

        assertThat(service.hasPermission("task:read")).isTrue();
    }

    @Test
    void shouldEvaluatePermissionContext() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        stubAuthorization("kc-user-1", userId, Set.of("role:create"));

        assertThat(service.hasPermission(
                authentication,
                PermissionEvaluationContext.of("role", "create")
        )).isTrue();
    }

    @Test
    void shouldReturnFalseForMissingInternalUser() {
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());

        assertThat(service.hasPermission(authentication, "user:read")).isFalse();
        verifyNoInteractions(rbacAuthorizationService);
    }

    @Test
    void shouldReturnFalseWhenAuthorizationCannotBeLoaded() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        User user = activeUser(userId);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));
        when(rbacAuthorizationService.getAuthorizationSnapshot(userId))
                .thenThrow(new UserInactiveException(AccountStatus.DISABLED));

        assertThat(service.hasPermission(authentication, "user:read")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenInternalUserIsInactive() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        User user = activeUser(userId);
        user.setStatus(AccountStatus.LOCKED);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        assertThat(service.hasPermission(authentication, "user:read")).isFalse();
        assertThat(service.isCurrentUser(authentication, userId)).isFalse();
        verifyNoInteractions(rbacAuthorizationService);
    }

    @Test
    void shouldReturnFalseWhenJwtWasIssuedBeforeUserTokenCutoff() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        User user = activeUser(userId);
        user.setTokenValidAfter(OffsetDateTime.now().plusMinutes(1));

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        assertThat(service.hasPermission(authentication, "user:read")).isFalse();
        assertThat(service.isCurrentUser(authentication, userId)).isFalse();
        verifyNoInteractions(rbacAuthorizationService);
    }

    @Test
    void shouldEvaluateAnyAndAllPermissionsUsingSingleAuthorizationSnapshot() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");
        stubAuthorization("kc-user-1", userId, Set.of("user:read", "task:*"));

        assertThat(service.hasAnyPermission(
                authentication,
                List.of("user:delete", "task:update")
        )).isTrue();
        assertThat(service.hasAllPermissions(
                authentication,
                List.of("user:read", "task:delete")
        )).isTrue();
        assertThat(service.hasAllPermissions(
                authentication,
                List.of("user:read", "role:create")
        )).isFalse();
    }

    @Test
    void shouldDetectCurrentUser() {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationToken authentication = jwtAuthentication("kc-user-1");

        when(userRepository.findByKeycloakId("kc-user-1"))
                .thenReturn(Optional.of(activeUser(userId)));

        assertThat(service.isCurrentUser(authentication, userId)).isTrue();
        assertThat(service.isCurrentUser(authentication, UUID.randomUUID())).isFalse();
    }

    private void stubAuthorization(
            String keycloakId,
            UUID userId,
            Set<String> permissions
    ) {
        User user = activeUser(userId);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(rbacAuthorizationService.getAuthorizationSnapshot(userId))
                .thenReturn(new UserAuthorizationSnapshot(
                        userId,
                        Set.of("user"),
                        permissions
                ));
    }

    private static User activeUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus(AccountStatus.ACTIVE);

        return user;
    }

    private static JwtAuthenticationToken jwtAuthentication(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", subject)
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("SCOPE_test"))
        );
    }
}
