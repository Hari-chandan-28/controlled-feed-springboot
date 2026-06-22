package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.service.FeedService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feed")
class FeedController(private val feedService: FeedService) {

    // Personalized feed by user genres — existing endpoint, unchanged
    @GetMapping
    fun getFeed(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<List<Video>> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        return ResponseEntity.ok(feedService.getFeed(email, page, pageSize))
    }

    // Category-specific paginated videos — fixes the tab pagination bug
    // Works for ANY sport: /api/feed/videos/category?category=BADMINTON
    @GetMapping("/videos/category")
    fun getVideosByCategory(
        @RequestParam category: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<Video>> {
        val cat = try { VideoCategory.valueOf(category.uppercase()) }
        catch (e: Exception) { return ResponseEntity.badRequest().build() }
        return ResponseEntity.ok(feedService.getVideosByCategory(cat, page, size))
    }

    // Random mix across all sports
    @GetMapping("/videos/random")
    fun getRandomFeed(
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<Video>> {
        return ResponseEntity.ok(feedService.getRandomFeed(size))
    }
}