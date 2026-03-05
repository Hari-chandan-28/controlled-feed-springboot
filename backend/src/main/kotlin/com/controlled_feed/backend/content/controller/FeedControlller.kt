package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.service.FeedService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/feed")
class FeedControlller (
    private val feedService: FeedService) {
    @GetMapping
    fun
            getFeed(@RequestParam(defaultValue = "0") page: Int,
                    @RequestParam(defaultValue = "10") pageSize: Int): ResponseEntity<List<Video>>
            {
            val email = SecurityContextHolder.getContext().authentication?.name
                ?:throw RuntimeException("Not authenticated")
                val videos = feedService.getFeed(email,page,pageSize)
                return ResponseEntity.ok(videos)
            }
}