package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.ObjectMapper

@Service
class YouTubeService (private val videoRepository: VideoRepository,
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
    fun fetchAndStoreF1Videos(): List<Video> {
        val response = fetchVideosByChannel(f1ChannelId, "F1")
        return parseAndSaveVideos(response, VideoCategory.F1)
    }
    fun fetchAndStoreICCVideos(): List<Video> {
        val response = fetchVideosByChannel(iccChannelId, "ICC")
        return parseAndSaveVideos(response, VideoCategory.CRICKET)
    }
    private fun fetchVideosByChannel(channelId: String, label: String):String {
        logger.info("Fetching $label videos from Youtube")
        val response = webClient.get()
            .uri{ uriBuilder->
            uriBuilder
            .path("/search")
            .queryParam("key", apiKey)
            .queryParam("channelId", channelId)
            .queryParam("part", "snippet")
            .queryParam("order","date")
            .queryParam("maxResult",10)
            .queryParam("type", "video")
            .build()
        }
        .retrieve()
            .bodyToMono(String::class.java)
        .block()
        logger.info("$label YouTubeResponse $response")
        return response?:""
    }
    private fun parseAndSaveVideos(response:String, category: VideoCategory): List<Video> {
        val savedVideos = mutableListOf<Video>()
        try{
            val root = objectMapper.readTree(response)
            val items = root.get("items")
            items.forEach { item ->
                val videoId = item.get("id")?.get("videoId")?.asString() ?: return@forEach
                val snippet = item.get("snippet")?:return@forEach
                if(videoRepository.existsByVideoId(videoId))
                {
                    logger.info("Video $videoId already exists")
                    return@forEach
                }
                val video = Video(
                    videoId=videoId,
                    title = snippet.get("title")?.asString()?:"",
                    description = snippet.get("description")?.asString()?:"",
                    thumbnailUrl = snippet.get("thumbnailUrl")
                        ?.get("high")
                        ?.get("url")?.asString()?:"",
                    publishedAt = snippet.get("publishedAt")?.asString()?:"",
                    channelTitle= snippet.get("channelTitle")?.asString()?:"",
                    category=category
                )

                savedVideos.add(videoRepository.save(video))
                logger.info("Saved video ${video.title}")
            }

        } catch(e: Exception)
        {
            logger.error("Error fetching videos from Youtube: ${e.message}")
        }
    return savedVideos
    }
}