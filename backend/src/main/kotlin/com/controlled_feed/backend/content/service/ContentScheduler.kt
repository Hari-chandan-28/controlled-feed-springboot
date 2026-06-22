package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ContentScheduler(
    private val youTubeService: YouTubeService,
    private val rssFeedService: RssFeedService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    // Fetch all YouTube channels every 6 hours
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    fun fetchAllYouTubeVideos() {
        logger.info("⏰ Scheduled: fetching all YouTube channels...")
        youTubeService.fetchAndStoreAllVideos()
    }

    // Fetch all RSS feeds every 2 hours
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    fun fetchAllRssFeeds() {
        logger.info("⏰ Scheduled: fetching all RSS feeds...")
        rssFeedService.fetchAndStoreAllArticles()
    }
}