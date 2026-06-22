package com.controlled_feed.backend.content.service

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service  // was missing — Spring won't pick this up without it
class VideoEventConsumer {
    private val logger = LoggerFactory.getLogger(VideoEventConsumer::class.java)

    @KafkaListener(topics = ["new-video"], groupId = "controlled_feed-group")
    fun consumeNewVideoEvent(message: String) {
        try {
            val parts = message.split(":")
            if (parts.size < 2) {
                logger.warn("⚠️ Malformed Kafka message: $message")
                return
            }
            val category = parts[0]
            val videoId = parts[1]

            // This now works for ALL sports automatically — no code change
            // needed when new sports are added since category is just a string
            logger.info("📥 New video event → Category: $category | VideoId: $videoId")

            // Future: trigger cache eviction, push notification, etc.
            // e.g. feedCacheService.evictCacheForCategory(category)

        } catch (e: Exception) {
            logger.error("❌ Failed to process Kafka message: $message | ${e.message}")
        }
    }
}