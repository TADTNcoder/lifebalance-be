package com.lifebalance.identity.service;

import static com.lifebalance.identity.config.RbacCacheConfig.USER_AUTHORIZATION_SNAPSHOTS_CACHE;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.lifebalance.identity.dto.UserAuthorizationSnapshot;
import com.lifebalance.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAuthorizationCacheService {

    private final CacheManager cacheManager;
    private final UserRepository userRepository;

    public Optional<UserAuthorizationSnapshot> get(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }

        Cache cache = cache();
        if (cache == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(cache.get(userId, UserAuthorizationSnapshot.class));
    }

    public void put(UUID userId, UserAuthorizationSnapshot snapshot) {
        if (userId == null || snapshot == null) {
            return;
        }

        Cache cache = cache();
        if (cache != null) {
            cache.put(userId, snapshot);
        }
    }

    public void evictUser(UUID userId) {
        if (userId == null) {
            return;
        }

        afterCommitOrNow(() -> doEvictUser(userId));
    }

    public void evictUsersByRoleId(UUID roleId) {
        if (roleId == null) {
            return;
        }

        afterCommitOrNow(() -> userRepository.findUserIdsByRoleId(roleId)
                .forEach(this::doEvictUser));
    }

    public void evictUsersByPermissionId(UUID permissionId) {
        if (permissionId == null) {
            return;
        }

        afterCommitOrNow(() -> userRepository.findUserIdsByPermissionId(permissionId)
                .forEach(this::doEvictUser));
    }

    public void evictAll() {
        afterCommitOrNow(() -> {
            Cache cache = cache();
            if (cache != null) {
                cache.clear();
            }
        });
    }

    private void doEvictUser(UUID userId) {
        Cache cache = cache();
        if (cache != null) {
            cache.evict(userId);
        }
    }

    private Cache cache() {
        return cacheManager.getCache(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
    }

    private static void afterCommitOrNow(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
