package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Article
import com.controlled_feed.backend.content.service.RssFeedService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rss")
class RssFeedController (private val rssFeedService: RssFeedService) {
    @GetMapping("/f1")
    fun fetchF1Articles(): ResponseEntity<List<Article>> {
        return ResponseEntity.ok(rssFeedService.fetchAndStoreF1Articles())
    }
    @GetMapping("/cricket")
    fun fetchCricketArticles(): ResponseEntity<List<Article>> {
        return ResponseEntity.ok(rssFeedService.fetchAndStoreCricketArticles())
    }
}