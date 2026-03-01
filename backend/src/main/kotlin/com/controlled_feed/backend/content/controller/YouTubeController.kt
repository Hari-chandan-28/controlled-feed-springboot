package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.service.YouTubeService
import org.hibernate.validator.internal.util.logging.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/youtube")
class YouTubeController (private val youTubeService: YouTubeService) {
    @GetMapping("/f1")
    fun fetchF1Videos(): ResponseEntity<List<Video>>{
        val videos = youTubeService.fetchAndStoreF1Videos()
        return ResponseEntity.ok(videos)
    }
    @GetMapping("/icc")
    fun fetchICCVideos(): ResponseEntity<List<Video>>{
        val videos = youTubeService.fetchAndStoreICCVideos()
        return ResponseEntity.ok(videos)
    }
}