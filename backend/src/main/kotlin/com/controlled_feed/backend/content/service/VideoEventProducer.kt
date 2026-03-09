package com.controlled_feed.backend.content.service

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class VideoEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(VideoEventProducer::class.java)

    fun sendNewVideoEvent(videoId: String, category: String) {
        val message = "$category:$videoId"
        kafkaTemplate.send("new-video", message)
        logger.info("📤 Sent Kafka event: $message")
    }
}