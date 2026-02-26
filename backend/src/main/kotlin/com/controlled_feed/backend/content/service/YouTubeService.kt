package com.controlled_feed.backend.content.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class YouTubeService {
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
    fun fetchF1Videos(): String {
        return fetchVideosByChannel(f1ChannelId,"F1")
    }
    fun fetchICCVideos():String{
        return fetchVideosByChannel(iccChannelId,"ICC")
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
}