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
            // F1 season data → 1 hour (doesn't change often)
            "f1-standings" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "f1-constructors" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "f1-results" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "f1-schedule" to defaultConfig.entryTtl(Duration.ofHours(1)),

            // Sessions/meetings only update once a day per OpenF1 docs —
            // cache for HOURS, not minutes. This is the main fix.
            "f1-session-context" to defaultConfig.entryTtl(Duration.ofHours(3)),
            "f1-meetings" to defaultConfig.entryTtl(Duration.ofHours(3)),

            // Circuit layout NEVER changes for a given circuit+year —
            // safe to cache very long
            "circuit-layout" to defaultConfig.entryTtl(Duration.ofHours(12)),

            // Driver grid is stable for an entire session weekend
            "f1-current-drivers" to defaultConfig.entryTtl(Duration.ofMinutes(30)),

            // F1 Live data → actually updates every ~4s during a race,
            // matches our SSE interval
            "f1-live-positions" to defaultConfig.entryTtl(Duration.ofSeconds(4)),
            "f1-live-timing" to defaultConfig.entryTtl(Duration.ofSeconds(4)),
            "f1-live-intervals" to defaultConfig.entryTtl(Duration.ofSeconds(4)),
            // F1 race details and schedules
            "f1-race-detail" to defaultConfig.entryTtl(Duration.ofDays(1)),

            "article-feed" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "feed" to defaultConfig.entryTtl(Duration.ofMinutes(5)),

            "cricket-live" to defaultConfig.entryTtl(Duration.ofSeconds(30)),
            "cricket-scorecard" to defaultConfig.entryTtl(Duration.ofSeconds(30)),
            "cricket-upcoming" to defaultConfig.entryTtl(Duration.ofHours(1))
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}