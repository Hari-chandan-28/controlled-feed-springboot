package com.controlled_feed.backend.content.controller

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
    fun getF1Videos(): ResponseEntity<String>{
        val respose = youTubeService.fetchF1Videos()
        return ResponseEntity.ok(respose)
    }
    @GetMapping("/icc")
    fun getICCVideos(): ResponseEntity<String>{
        val respose = youTubeService.fetchICCVideos()
        return ResponseEntity.ok(respose)
    }
}