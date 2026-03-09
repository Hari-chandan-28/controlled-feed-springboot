package com.controlled_feed.backend.content.service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.jvm.java
import org.slf4j.LoggerFactory


@Component
class VideoScheduler(private val youTubeService: YouTubeService,
    private val feedService: FeedService,
    private val rssFeedService: RssFeedService) {
    private val logger = LoggerFactory.getLogger(VideoScheduler::class.java)
    @Scheduled(fixedRate = 600000)
    fun fetchVideos() {
        logger.info("Scheduler started - Fetching latest videos..")
        val f1Videos = youTubeService.fetchAndStoreF1Videos()
        logger.info("F1 videos fetched: ${f1Videos.size} new videos")
        val iccVideos = youTubeService.fetchAndStoreICCVideos()
        logger.info("ICC videos fetched: ${iccVideos.size}new videos")
        val f1Articles = rssFeedService.fetchAndStoreF1Articles()
        logger.info("📰 F1 articles fetched: ${f1Articles.size} new articles")
        val cricketArticles = rssFeedService.fetchAndStoreCricketArticles()
        logger.info("📰 Cricket articles fetched: ${cricketArticles.size} new articles")
        feedService.clearFeedCache()
        logger.info("Feed cache cleared")

        logger.info("Scheduler completed")
    }

}