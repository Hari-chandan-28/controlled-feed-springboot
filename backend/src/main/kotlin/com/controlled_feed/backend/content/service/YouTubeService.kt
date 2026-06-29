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
) {
    private val logger = LoggerFactory.getLogger(YouTubeService::class.java)

    @Value("\${youtube.api.key}")
    lateinit var apiKey: String

    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://www.googleapis.com/youtube/v3")
        .build()

    // ── Convert channel ID → uploads playlist ID ──────────
    // Channel: UCxxxxxx → Playlist: UUxxxxxx (C→U at index 1)
    // This is a guaranteed YouTube convention, no API call needed


    // ── Fetch all channels, staggered to avoid rate limits ─
    fun fetchAndStoreAllVideos(): List<Video> {
        logger.info("🔄 Fetching videos for all ${SportRegistry.allChannels.size} channels...")
        val allSaved = mutableListOf<Video>()

        SportRegistry.allChannels.forEachIndexed { index, channel ->
            try {
                logger.info("📺 [${index + 1}/${SportRegistry.allChannels.size}] Fetching ${channel.channelName}...")
                val saved = fetchAndStoreChannel(
                    channel.channelId,
                    channel.channelName,
                    channel.category
                )
                allSaved.addAll(saved)

                // Stagger: wait 2 seconds between each channel
                // prevents burst rate limiting from YouTube
                if (index < SportRegistry.allChannels.size - 1) {
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                logger.error("❌ Failed ${channel.channelName}: ${e.message}")
                // Continue to next channel even if one fails
            }
        }

        logger.info("✅ Total saved: ${allSaved.size} videos across all channels")
        return allSaved
    }

    // ── Fetch one sport's channels, staggered ─────────────
    fun fetchAndStoreSport(category: VideoCategory): List<Video> {
        val sport = SportRegistry.byCategory[category] ?: return emptyList()
        logger.info("🔄 Fetching ${sport.displayName} (${sport.channels.size} channels)...")
        val allSaved = mutableListOf<Video>()

        sport.channels.forEachIndexed { index, channel ->
            try {
                val saved = fetchAndStoreChannel(
                    channel.channelId,
                    channel.channelName,
                    channel.category
                )
                allSaved.addAll(saved)
                if (index < sport.channels.size - 1) {
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                logger.error("❌ Failed ${channel.channelName}: ${e.message}")
            }
        }
        return allSaved
    }

    // ── In YouTubeService.kt ───────────────────────────────────

    // In-memory cache — populated once per channel per app restart
// After first lookup, zero extra API calls needed
    private val uploadsPlaylistCache = mutableMapOf<String, String>()

    private fun getUploadsPlaylistId(channelId: String, label: String): String? {
        // Return cached if already looked up this session
        uploadsPlaylistCache[channelId]?.let {
            logger.info("📋 Using cached playlist ID for $label: $it")
            return it
        }

        return try {
            logger.info("🔍 Looking up uploads playlist for $label ($channelId)...")
            val response = webClient.get()
                .uri { builder ->
                    builder.path("/channels")
                        .queryParam("key", apiKey)
                        .queryParam("id", channelId)
                        .queryParam("part", "contentDetails")
                        .build()
                }
                .retrieve()
                .bodyToMono(String::class.java)
                .block() ?: run {
                logger.error("❌ Empty response for channel lookup: $label")
                return null
            }

            val root = objectMapper.readTree(response)
            val items = root.get("items")

            if (items == null || items.size() == 0) {
                logger.error("❌ Channel not found: $label ($channelId) — check channel ID is correct")
                return null
            }

            val playlistId = items
                .firstOrNull()
                ?.get("contentDetails")
                ?.get("relatedPlaylists")
                ?.get("uploads")
                ?.asText()

            if (playlistId.isNullOrEmpty()) {
                logger.error("❌ No uploads playlist found for $label")
                return null
            }

            logger.info("✅ Found uploads playlist for $label: $playlistId")
            uploadsPlaylistCache[channelId] = playlistId
            playlistId

        } catch (e: Exception) {
            logger.error("❌ Channel lookup failed for $label: ${e.message}")
            null
        }
    }

    fun fetchAndStoreChannel(
        channelId: String,
        label: String,
        category: VideoCategory
    ): List<Video> {
        // Always use the API to get the real uploads playlist ID
        // NOT the UC→UU trick — that doesn't work for all channels
        val playlistId = getUploadsPlaylistId(channelId, label)
        if (playlistId == null) {
            logger.warn("⚠️ Skipping $label — could not resolve uploads playlist")
            return emptyList()
        }
        val response = fetchWithRetry(playlistId, label)
        val saved = parseAndSaveVideos(response, category, channelId)
        pruneOldVideos(channelId)
        return saved
    }

    // ── Fetch from playlistItems endpoint (1 unit cost) ───
    private fun fetchVideosByPlaylist(playlistId: String, label: String): String {
        logger.info("🎬 Fetching playlist $playlistId for $label")
        val response = webClient.get()
            .uri { builder ->
                builder.path("/playlistItems")
                    .queryParam("key", apiKey)
                    .queryParam("playlistId", playlistId)
                    .queryParam("part", "snippet")
                    .queryParam("maxResults", 20)
                    .build()
            }
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) { res ->
                res.bodyToMono(String::class.java).flatMap { body ->
                    logger.error("❌ YouTube API error for $label: $body")
                    reactor.core.publisher.Mono.error(RuntimeException("YouTube API error: $body"))
                }
            }
            .bodyToMono(String::class.java)
            .block()
        logger.info("✅ $label playlist response received")
        return response ?: ""
    }

    // ── Retry with exponential backoff ────────────────────
    private fun fetchWithRetry(playlistId: String, label: String): String {
        var attempt = 1
        var delay = 3000L
        while (attempt <= 3) {
            try {
                return fetchVideosByPlaylist(playlistId, label)
            } catch (e: Exception) {
                logger.error("❌ $label attempt $attempt failed: ${e.message}")
                if (attempt == 3) throw e
                logger.info("⏳ Waiting ${delay}ms before retry...")
                Thread.sleep(delay)
                delay *= 2  // 3s → 6s → 12s
                attempt++
            }
        }
        throw RuntimeException("All retries failed for $label")
    }

    // ── Parse playlistItems response (different shape from search) ─
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
                val snippet = item.get("snippet") ?: return@forEach

                // playlistItems returns videoId inside resourceId
                val videoId = snippet.get("resourceId")
                    ?.get("videoId")?.asText() ?: return@forEach

                // Skip deleted/private videos
                val title = snippet.get("title")?.asText() ?: return@forEach
                if (title == "Deleted video" || title == "Private video") return@forEach

                if (videoRepository.existsByVideoId(videoId)) return@forEach

                val description = snippet.get("description")?.asText() ?: ""

                val resolvedCategory = if (category == VideoCategory.MIXED) {
                    SportRegistry.detectCategory(title, description)
                } else {
                    category
                }

                val thumbnailUrl = snippet.get("thumbnails")
                    ?.let { thumbs ->
                        // Try high → medium → default in order
                        thumbs.get("high")?.get("url")?.asText()
                            ?: thumbs.get("medium")?.get("url")?.asText()
                            ?: thumbs.get("default")?.get("url")?.asText()
                            ?: ""
                    } ?: ""

                val publishedAt = snippet.get("publishedAt")?.asText()
                    ?: snippet.get("videoOwnerChannelTitle")?.asText()
                    ?: ""

                val channelTitle = snippet.get("videoOwnerChannelTitle")?.asText()
                    ?: snippet.get("channelTitle")?.asText()
                    ?: ""

                val video = Video(
                    videoId = videoId,
                    title = title,
                    description = description,
                    thumbnailUrl = thumbnailUrl,
                    publishedAt = publishedAt,
                    channelTitle = channelTitle,
                    channelId = channelId,
                    category = resolvedCategory
                )
                savedVideos.add(videoRepository.save(video))
                logger.info("✅ Saved: $title [$resolvedCategory]")
            }
        } catch (e: Exception) {
            logger.error("❌ Error parsing response: ${e.message}")
        }
        return savedVideos
    }

    // ── Prune: keep 20 newest per channel ─────────────────
    private fun pruneOldVideos(channelId: String) {
        val all = videoRepository.findByChannelIdOrderByPublishedAtDesc(channelId)
        if (all.size > 20) {
            val toDelete = all.drop(20)
            videoRepository.deleteAll(toDelete)
            logger.info("🗑️ Pruned ${toDelete.size} old videos for $channelId")
        }
    }

    fun fallbackAllVideos(e: Exception): List<Video> {
        logger.error("⚡ Circuit OPEN: ${e.message}")
        return emptyList()
    }
}