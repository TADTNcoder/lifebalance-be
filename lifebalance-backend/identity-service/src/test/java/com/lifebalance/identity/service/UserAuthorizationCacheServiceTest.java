package com.lifebalance.identity.service;

import static com.lifebalance.identity.config.RbacCacheConfig.USER_AUTHORIZATION_SNAPSHOTS_CACHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationCacheServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserAuthorizationCacheService service;

    @BeforeEach
    void setUp() {
        CacheManager cacheManager =
                new ConcurrentMapCacheManager(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
        service = new UserAuthorizationCacheService(cacheManager, userRepository);
    }

    @Test
    void shouldReadSnapshotFromCacheAfterPut() {
        UUID userId = UUID.randomUUID();
        UserAuthorizationSnapshot snapshot = snapshot(userId, "user", "task:read");

        service.put(userId, snapshot);

        assertThat(service.get(userId)).contains(snapshot);
    }

    @Test
    void shouldEvictUserSnapshot() {
        UUID userId = UUID.randomUUID();
        service.put(userId, snapshot(userId, "user", "task:read"));

        service.evictUser(userId);

        assertThat(service.get(userId)).isEmpty();
    }

    @Test
    void shouldEvictAffectedUsersByRoleId() {
        UUID roleId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();
        service.put(firstUserId, snapshot(firstUserId, "manager", "task:read"));
        service.put(secondUserId, snapshot(secondUserId, "manager", "task:write"));
        when(userRepository.findUserIdsByRoleId(roleId))
                .thenReturn(java.util.List.of(firstUserId, secondUserId));

        service.evictUsersByRoleId(roleId);

        assertThat(service.get(firstUserId)).isEmpty();
        assertThat(service.get(secondUserId)).isEmpty();
        verify(userRepository).findUserIdsByRoleId(roleId);
    }

    @Test
    void shouldEvictAffectedUsersByPermissionId() {
        UUID permissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        service.put(userId, snapshot(userId, "manager", "task:delete"));
        when(userRepository.findUserIdsByPermissionId(permissionId))
                .thenReturn(java.util.List.of(userId));

        service.evictUsersByPermissionId(permissionId);

        assertThat(service.get(userId)).isEmpty();
        verify(userRepository).findUserIdsByPermissionId(permissionId);
    }

    private static UserAuthorizationSnapshot snapshot(
            UUID userId,
            String role,
            String permission
    ) {
        return new UserAuthorizationSnapshot(
                userId,
                Set.of(role),
                Set.of(permission)
        );
    }
}
