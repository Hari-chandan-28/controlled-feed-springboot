package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.service.YouTubeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/youtube")
class YouTubeController(private val youTubeService: YouTubeService) {

    // Fetch videos for one sport — works for ANY sport
    // e.g. /api/youtube/fetch?category=TENNIS
    @GetMapping("/fetch")
    fun fetchBySport(@RequestParam category: String): ResponseEntity<List<Video>> {
        val cat = try { VideoCategory.valueOf(category.uppercase()) }
        catch (e: Exception) { return ResponseEntity.badRequest().build() }
        return ResponseEntity.ok(youTubeService.fetchAndStoreSport(cat))
    }

    // Fetch ALL sports at once
    @GetMapping("/fetch/all")
    fun fetchAll(): ResponseEntity<Map<String, Int>> {
        val results = youTubeService.fetchAndStoreAllVideos()
        return ResponseEntity.ok(mapOf("saved" to results.size))
    }
}