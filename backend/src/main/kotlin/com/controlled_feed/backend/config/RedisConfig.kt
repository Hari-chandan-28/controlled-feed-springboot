package com.controlled_feed.backend.config

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(RedisSerializer.java())
            )
        val cacheConfigurations = mapOf(
            // F1 Season data → cache for 1 hour (doesn't change often)
            "f1-standings" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "f1-constructors" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "f1-results" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "f1-schedule" to defaultConfig.entryTtl(Duration.ofHours(1)),

            // F1 Live data → cache for 3 seconds (updates frequently)
            "f1-live-positions" to defaultConfig.entryTtl(Duration.ofSeconds(3)),
            "f1-live-timing" to defaultConfig.entryTtl(Duration.ofSeconds(3)),
            "f1-live-intervals" to defaultConfig.entryTtl(Duration.ofSeconds(3)),

            // Feed cache → 5 minutes
            "feed" to defaultConfig.entryTtl(Duration.ofMinutes(5))
        )
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}