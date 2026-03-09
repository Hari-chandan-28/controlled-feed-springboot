package com.controlled_feed.backend.content.service

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener

class VideoEventConsumer {
    private val logger = LoggerFactory.getLogger(VideoEventConsumer::class.java)
    @KafkaListener(topics = ["new-video"],groupId="controlled_feed-group")
    fun consumeNewVideoEvent(message: String) {
        val parts=message.split(":")
        val category=parts[0]
        val videoId = parts[1]
        logger.info("Received Kafka event -> Category: $category, VideoId: $videoId")
        logger.info("New $category video available: $videoId")
    }
}