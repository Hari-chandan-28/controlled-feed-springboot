package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.repository.VideoRepository
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.spring6.fallback.FallbackMethod
import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class YouTubeService(
    private val videoRepository: VideoRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(YouTubeService::class.java)
    @Value("\${youtube.api.key}")
    lateinit var apiKey: String
    @Value("\${youtube.f1.channel.id}")
    lateinit var f1ChannelId: String
    @Value("\${youtube.icc.channel.id}")
    lateinit var iccChannelId: String

    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://www.googleapis.com/youtube/v3")
        .build()
    @CircuitBreaker(name = "youtubeService" , fallbackMethod = "fallbackF1Videos")
    fun fetchAndStoreF1Videos(): List<Video> {
        logger.info("🔄 Attempting to fetch F1 videos...")
        val response = fetchWithRetry(f1ChannelId, "F1")
        return parseAndSaveVideos(response, VideoCategory.F1)
    }
    @CircuitBreaker(name = "youtubeService", fallbackMethod = "fallbackICCVideos")

    fun fetchAndStoreICCVideos(): List<Video> {
        logger.info("🔄 Attempting to fetch ICC videos...")
        val response = fetchWithRetry(iccChannelId, "ICC")
        return parseAndSaveVideos(response, VideoCategory.CRICKET)
    }
    private fun fetchWithRetry(channelId: String, label: String): String {
        var attempt = 1
        var delay = 2000L
        while (attempt <= 3) {
            try{
                logger.info("🔄 $label attempt $attempt...")
                val response = fetchVideosByChannel(channelId, label)
                return response
            }
            catch (e: Exception){
                logger.error("❌ $label attempt $attempt failed: ${e.message}")
                if (attempt == 3) throw e
                Thread.sleep(delay)
                delay *= 2
                attempt++
            }
        }
        throw RuntimeException("All retries failed for $label")
    }
    fun fallbackF1Videos(e: Exception): List<Video> {
        logger.error("⚡ Circuit OPEN for F1! Returning empty. Error: ${e.message}")
        return emptyList()
    }
    fun fallbackICCVideos(e: Exception): List<Video> {
        logger.error("⚡ Circuit OPEN for ICC! Returning empty. Error: ${e.message}")
        return emptyList()
    }
    fun fetchVideosByChannel(channelId: String, label: String): String {
        logger.info("Fetching $label videos from YouTube...")
        val response = webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/search")
                    .queryParam("key", apiKey)
                    .queryParam("channelId", channelId)
                    .queryParam("part", "snippet")
                    .queryParam("order", "date")
                    .queryParam("maxResults", 10)
                    .queryParam("type", "video")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
        logger.info("✅ $label YouTube Response received!")
        return response ?: ""
    }
    private fun parseAndSaveVideos(response: String, category: VideoCategory): List<Video> {
        if (response.isEmpty()) return emptyList()
        val savedVideos = mutableListOf<Video>()
        try {
            val root = objectMapper.readTree(response)
            val items = root.get("items")
            items?.forEach { item ->
                val videoId = item.get("id")?.get("videoId")?.asText() ?: return@forEach
                val snippet = item.get("snippet") ?: return@forEach
                if (videoRepository.existsByVideoId(videoId)) {
                    logger.info("Video already exists: $videoId")
                    return@forEach
                }
                val video = Video(
                    videoId = videoId,
                    title = snippet.get("title")?.asString() ?: "",
                    description = snippet.get("description")?.asString() ?: "",
                    thumbnailUrl = snippet.get("thumbnails")
                        ?.get("high")
                        ?.get("url")?.asString() ?: "",
                    publishedAt = snippet.get("publishedAt")?.asString() ?: "",
                    channelTitle = snippet.get("channelTitle")?.asString() ?: "",
                    category = category
                )
                savedVideos.add(videoRepository.save(video))
                logger.info("Saved video: ${video.title}")
            }
        } catch (e: Exception) {
            logger.error("Error parsing YouTube response: ${e.message}")
        }
        return savedVideos
    }
}