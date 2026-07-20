package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.dto.CheckPermissionResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnCurrentRolesPermissionsAndRequestedPermissionResult() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setKeycloakId("kc-user-1");
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setDisplayName("Alice");
        user.setStatus(AccountStatus.ACTIVE);

        CurrentUser currentUser = new CurrentUser(
                "kc-user-1",
                "alice",
                "alice@example.com",
                List.of("user", "task:read")
        );

        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of("USER"));
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of("task:read", "task:write"));

        AuthorizationServiceImpl service =
                new AuthorizationServiceImpl(userRepository);

        CheckPermissionResponse response =
                service.checkPermission(user, currentUser, "TASK:READ");

        assertThat(response.authenticated()).isTrue();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.keycloakId()).isEqualTo("kc-user-1");
        assertThat(response.tokenRoles()).containsExactly("task:read", "user");
        assertThat(response.roles()).containsExactly("USER");
        assertThat(response.permissions())
                .containsExactly("task:read", "task:write");
        assertThat(response.requestedPermission()).isEqualTo("TASK:READ");
        assertThat(response.hasPermission()).isTrue();
    }

    @Test
    void shouldLeavePermissionCheckNullWhenPermissionIsNotRequested() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        CurrentUser currentUser = new CurrentUser(
                "kc-user-1",
                "alice",
                "alice@example.com",
                null
        );

        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of());
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of());

        AuthorizationServiceImpl service =
                new AuthorizationServiceImpl(userRepository);

        CheckPermissionResponse response =
                service.checkPermission(user, currentUser, " ");

        assertThat(response.tokenRoles()).isEmpty();
        assertThat(response.roles()).isEmpty();
        assertThat(response.permissions()).isEmpty();
        assertThat(response.requestedPermission()).isNull();
        assertThat(response.hasPermission()).isNull();
    }
}
