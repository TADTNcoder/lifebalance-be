package com.lifebalance.identity.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RbacCacheConfig {

    public static final String USER_AUTHORIZATION_SNAPSHOTS_CACHE = "userAuthorizationSnapshots";

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager rbacCacheManager() {
        return new ConcurrentMapCacheManager(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
    }
}
