package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.config.SportRegistry
import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.repository.VideoRepository
import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class YouTubeService(
    private val videoRepository: VideoRepository,
    private val objectMapper: ObjectMapper,
    private val videoEventProducer: VideoEventProducer
) {
    private val logger = LoggerFactory.getLogger(YouTubeService::class.java)

    @Value("\${youtube.api.key}")
    lateinit var apiKey: String

    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://www.googleapis.com/youtube/v3")
        .build()

    // ── Fetch all channels across all sports ──────────────────
    fun fetchAndStoreAllVideos(): List<Video> {
        logger.info("🔄 Fetching videos for all ${SportRegistry.allChannels.size} channels...")
        return SportRegistry.allChannels.flatMap { channel ->
            try {
                fetchAndStoreChannel(channel.channelId, channel.channelName, channel.category)
            } catch (e: Exception) {
                logger.error("❌ Failed to fetch ${channel.channelName}: ${e.message}")
                emptyList()
            }
        }
    }

    // ── Fetch a single sport's channels ──────────────────────
    fun fetchAndStoreSport(category: VideoCategory): List<Video> {
        val sport = SportRegistry.byCategory[category]
            ?: return emptyList()
        logger.info("🔄 Fetching ${sport.displayName} videos from ${sport.channels.size} channels...")
        return sport.channels.flatMap { channel ->
            try {
                fetchAndStoreChannel(channel.channelId, channel.channelName, channel.category)
            } catch (e: Exception) {
                logger.error("❌ Failed to fetch ${channel.channelName}: ${e.message}")
                emptyList()
            }
        }
    }

    // ── Core: fetch one channel, save, cleanup old ────────────
    fun fetchAndStoreChannel(
        channelId: String,
        label: String,
        category: VideoCategory
    ): List<Video> {
        logger.info("📺 Fetching $label ($channelId)...")
        val response = fetchWithRetry(channelId, label)
        val saved = parseAndSaveVideos(response, category, channelId)

        // Keep only 20 most recent per channel, delete the rest
        pruneOldVideos(channelId)

        return saved
    }

    // ── Prune: keep 20 newest per channel ────────────────────
    private fun pruneOldVideos(channelId: String) {
        val allForChannel = videoRepository
            .findByChannelIdOrderByPublishedAtDesc(channelId)
        if (allForChannel.size > 20) {
            val toDelete = allForChannel.drop(20)
            videoRepository.deleteAll(toDelete)
            logger.info("🗑️ Pruned ${toDelete.size} old videos for channel $channelId")
        }
    }

    // ── Retry logic ───────────────────────────────────────────
    private fun fetchWithRetry(channelId: String, label: String): String {
        var attempt = 1
        var delay = 2000L
        while (attempt <= 3) {
            try {
                return fetchVideosByChannel(channelId, label)
            } catch (e: Exception) {
                logger.error("❌ $label attempt $attempt failed: ${e.message}")
                if (attempt == 3) throw e
                Thread.sleep(delay)
                delay *= 2
                attempt++
            }
        }
        throw RuntimeException("All retries failed for $label")
    }

    private fun fetchVideosByChannel(channelId: String, label: String): String {
        val response = webClient.get()
            .uri { builder ->
                builder.path("/search")
                    .queryParam("key", apiKey)
                    .queryParam("channelId", channelId)
                    .queryParam("part", "snippet")
                    .queryParam("order", "date")
                    .queryParam("maxResults", 20)
                    .queryParam("type", "video")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
        logger.info("✅ $label YouTube response received")
        return response ?: ""
    }

    private fun parseAndSaveVideos(
        response: String,
        category: VideoCategory,
        channelId: String
    ): List<Video> {
        if (response.isEmpty()) return emptyList()
        val savedVideos = mutableListOf<Video>()
        try {
            val root = objectMapper.readTree(response)
            val items = root.get("items") ?: return emptyList()
            items.forEach { item ->
                val videoId = item.get("id")?.get("videoId")?.asText() ?: return@forEach
                val snippet = item.get("snippet") ?: return@forEach

                if (videoRepository.existsByVideoId(videoId)) return@forEach

                val title = snippet.get("title")?.asText() ?: ""
                val description = snippet.get("description")?.asText() ?: ""

                // If MIXED channel, detect actual sport from title/description
                val resolvedCategory = if (category == VideoCategory.MIXED) {
                    SportRegistry.detectCategory(title, description)
                } else {
                    category
                }

                val video = Video(
                    videoId = videoId,
                    title = title,
                    description = description,
                    thumbnailUrl = snippet.get("thumbnails")
                        ?.get("high")?.get("url")?.asText() ?: "",
                    publishedAt = snippet.get("publishedAt")?.asText() ?: "",
                    channelTitle = snippet.get("channelTitle")?.asText() ?: "",
                    channelId = channelId,
                    category = resolvedCategory
                )
                savedVideos.add(videoRepository.save(video))
                videoEventProducer.sendNewVideoEvent(video.videoId, resolvedCategory.name)
                logger.info("✅ Saved: ${video.title} [$resolvedCategory]")
            }
        } catch (e: Exception) {
            logger.error("Error parsing YouTube response: ${e.message}")
        }
        return savedVideos
    }

    // ── Fallbacks ─────────────────────────────────────────────
    fun fallbackAllVideos(e: Exception): List<Video> {
        logger.error("⚡ Circuit OPEN for YouTube fetch: ${e.message}")
        return emptyList()
    }
}