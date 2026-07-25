package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.dto.CheckPermissionResponse;
import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.RbacAuthorizationService;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock
    private RbacAuthorizationService rbacAuthorizationService;

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

        UserAuthorizationSnapshot authorization = new UserAuthorizationSnapshot(
                userId,
                linkedSet("user"),
                linkedSet("task:read", "task:write")
        );
        when(rbacAuthorizationService.getAuthorizationSnapshot(userId))
                .thenReturn(authorization);
        when(rbacAuthorizationService.hasPermission(authorization, "task:read"))
                .thenReturn(true);

        AuthorizationServiceImpl service =
                new AuthorizationServiceImpl(rbacAuthorizationService);

        CheckPermissionResponse response =
                service.checkPermission(user, currentUser, "TASK:READ");

        assertThat(response.authenticated()).isTrue();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.keycloakId()).isEqualTo("kc-user-1");
        assertThat(response.tokenRoles()).containsExactly("task:read", "user");
        assertThat(response.roles()).containsExactly("user");
        assertThat(response.permissions())
                .containsExactly("task:read", "task:write");
        assertThat(response.requestedPermission()).isEqualTo("task:read");
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

        when(rbacAuthorizationService.getAuthorizationSnapshot(userId))
                .thenReturn(new UserAuthorizationSnapshot(userId, Set.of(), Set.of()));

        AuthorizationServiceImpl service =
                new AuthorizationServiceImpl(rbacAuthorizationService);

        CheckPermissionResponse response =
                service.checkPermission(user, currentUser, " ");

        assertThat(response.tokenRoles()).isEmpty();
        assertThat(response.roles()).isEmpty();
        assertThat(response.permissions()).isEmpty();
        assertThat(response.requestedPermission()).isNull();
        assertThat(response.hasPermission()).isNull();
    }

    private static Set<String> linkedSet(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }
}
