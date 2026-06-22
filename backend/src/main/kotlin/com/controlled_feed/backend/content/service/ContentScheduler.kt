package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.VideoCategory
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

    // Stagger YouTube fetches — one sport every 2 hours
    // so we never hit more than ~5 channels at once
    @Scheduled(cron = "0 0 */2 * * *")      // every 2 hours at :00
    fun fetchF1Videos() {
        logger.info("⏰ Scheduled F1 YouTube fetch")
        youTubeService.fetchAndStoreSport(VideoCategory.F1)
    }

    @Scheduled(cron = "0 30 */2 * * *")     // every 2 hours at :30
    fun fetchCricketVideos() {
        logger.info("⏰ Scheduled Cricket YouTube fetch")
        youTubeService.fetchAndStoreSport(VideoCategory.CRICKET)
    }

    @Scheduled(cron = "0 0 1-23/3 * * *")   // every 3 hours offset by 1
    fun fetchFootballVideos() {
        logger.info("⏰ Scheduled Football YouTube fetch")
        youTubeService.fetchAndStoreSport(VideoCategory.FOOTBALL)
    }

    @Scheduled(cron = "0 0 2-23/4 * * *")   // every 4 hours offset by 2
    fun fetchTennisVideos() {
        logger.info("⏰ Scheduled Tennis YouTube fetch")
        youTubeService.fetchAndStoreSport(VideoCategory.TENNIS)
    }

    @Scheduled(cron = "0 0 3-23/4 * * *")   // every 4 hours offset by 3
    fun fetchBadmintonVideos() {
        logger.info("⏰ Scheduled Badminton YouTube fetch")
        youTubeService.fetchAndStoreSport(VideoCategory.BADMINTON)
    }

    // RSS feeds every 2 hours — cheap, no quota concerns
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    fun fetchAllRssFeeds() {
        logger.info("⏰ Scheduled RSS fetch for all sports")
        rssFeedService.fetchAndStoreAllArticles()
    }
}