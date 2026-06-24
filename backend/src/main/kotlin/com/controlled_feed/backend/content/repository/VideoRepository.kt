package com.controlled_feed.backend.content.repository

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface VideoRepository : JpaRepository<Video, Long> {
    fun existsByVideoId(videoId: String): Boolean
    fun findByCategoryIn(categories: List<VideoCategory>, pageable: Pageable): List<Video>
    fun findByCategory(category: VideoCategory, pageable: Pageable): List<Video>
    fun findByChannelIdOrderByPublishedAtDesc(channelId: String): List<Video>
    fun countByCategory(category: VideoCategory): Long
    fun findByCategoryIn(categories: List<VideoCategory>): List<Video>
}