package com.lifebalance.identity.service.impl;

import static com.lifebalance.identity.config.RbacCacheConfig.USER_AUTHORIZATION_SNAPSHOTS_CACHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.access.AccessDeniedException;

import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.service.UserAuthorizationCacheService;

@ExtendWith(MockitoExtension.class)
class RbacAuthorizationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private RbacAuthorizationServiceImpl service;

    @BeforeEach
    void setUp() {
        CacheManager cacheManager =
                new ConcurrentMapCacheManager(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
        UserAuthorizationCacheService userAuthorizationCacheService =
                new UserAuthorizationCacheService(cacheManager, userRepository);
        service = new RbacAuthorizationServiceImpl(userRepository, userAuthorizationCacheService);
    }

    @Test
    void shouldReturnTrueWhenUserHasRequiredRoleAndPermission() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of("USER"));
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of("task:read", "TASK:WRITE"));

        assertThat(service.hasRole(userId, " user ")).isTrue();
        assertThat(service.hasPermission(userId, "task:write")).isTrue();

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findRoleCodesByUserId(userId);
        verify(userRepository, times(1)).findPermissionCodesByUserId(userId);
    }

    @Test
    void shouldReturnFalseWhenUserMissingRoleOrPermission() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of("user"));
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of("task:read"));

        assertThat(service.hasRole(userId, "admin")).isFalse();
        assertThat(service.hasPermission(userId, "task:delete")).isFalse();
    }

    @Test
    void shouldDeduplicatePermissionsFromMultipleRoles() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of("USER", "admin", "user"));
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of("task:read", "TASK:READ", "task:write"));

        UserAuthorizationSnapshot authorization =
                service.getAuthorizationSnapshot(userId);

        assertThat(authorization.roles()).containsExactly("admin", "user");
        assertThat(authorization.permissions())
                .containsExactly("task:read", "task:write");
    }

    @Test
    void shouldThrowUserNotFoundWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserPermissions(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowAccessDeniedWhenPermissionIsRequiredButMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of("user"));
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of("task:read"));

        assertThatThrownBy(() -> service.requirePermission(userId, "task:delete"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("task:delete");
    }

    @Test
    void shouldRejectInactiveUserBeforeReturningAuthorization() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        user.setStatus(AccountStatus.DISABLED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getAuthorizationSnapshot(userId))
                .isInstanceOf(UserInactiveException.class);
    }

    @Test
    void shouldReloadAuthorizationAfterEviction() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(userRepository.findRoleCodesByUserId(userId))
                .thenReturn(List.of("user"))
                .thenReturn(List.of("admin"));
        when(userRepository.findPermissionCodesByUserId(userId))
                .thenReturn(List.of("task:read"))
                .thenReturn(List.of("task:write"));

        assertThat(service.getUserPermissions(userId)).containsExactly("task:read");

        service.evictUserAuthorization(userId);

        assertThat(service.getUserPermissions(userId)).containsExactly("task:write");
        verify(userRepository, times(2)).findById(userId);
        verify(userRepository, times(2)).findRoleCodesByUserId(userId);
        verify(userRepository, times(2)).findPermissionCodesByUserId(userId);
    }

    private static User activeUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus(AccountStatus.ACTIVE);

        return user;
    }
}
