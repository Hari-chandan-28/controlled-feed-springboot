package com.controlled_feed.backend.content.repository

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
interface VideoRepository : JpaRepository<Video, Long> {
    fun findByCategory(category: VideoCategory): List<Video>
    fun existsByVideoId(videoId: String): Boolean
    fun findByCategoryIn(categories: List<VideoCategory>, pageable: Pageable): List<Video>

}