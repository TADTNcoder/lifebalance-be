package com.lifebalance.identity.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class RbacCacheConfig {

    public static final String USER_AUTHORIZATION_SNAPSHOTS_CACHE = "userAuthorizationSnapshots";
    public static final String CACHE_KEY_PREFIX = "identity:rbac:";

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.rbac.cache",
            name = "type",
            havingValue = "redis"
    )
    CacheManager rbacRedisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            @Value("${lifebalance.rbac.cache.ttl:15m}") Duration ttl
    ) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(ttl)
                .prefixCacheNameWith(CACHE_KEY_PREFIX)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration(USER_AUTHORIZATION_SNAPSHOTS_CACHE, cacheConfiguration)
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lifebalance.rbac.cache",
            name = "type",
            havingValue = "caffeine",
            matchIfMissing = true
    )
    CacheManager rbacCaffeineCacheManager(
            @Value("${lifebalance.rbac.cache.ttl:15m}") Duration ttl,
            @Value("${lifebalance.rbac.cache.maximum-size:10000}") long maximumSize
    ) {
        CaffeineCacheManager cacheManager =
                new CaffeineCacheManager(USER_AUTHORIZATION_SNAPSHOTS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .recordStats());

        return cacheManager;
    }
}
