package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Article
import com.controlled_feed.backend.content.service.RssFeedService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rss")
class RssFeedController(private val rssFeedService: RssFeedService) {

    // Returns articles from DB
    @GetMapping("/f1")
    fun getF1Articles(): ResponseEntity<List<Article>> {
        return ResponseEntity.ok(rssFeedService.getF1Articles())
    }

    // Returns articles from DB
    @GetMapping("/cricket")
    fun getCricketArticles(): ResponseEntity<List<Article>> {
        return ResponseEntity.ok(rssFeedService.getCricketArticles())
    }

    // Manually trigger fetch (optional)
    @GetMapping("/fetch/f1")
    fun fetchF1Articles(): ResponseEntity<List<Article>> {
        return ResponseEntity.ok(rssFeedService.fetchAndStoreF1Articles())
    }

    @GetMapping("/fetch/cricket")
    fun fetchCricketArticles(): ResponseEntity<List<Article>> {
        return ResponseEntity.ok(rssFeedService.fetchAndStoreCricketArticles())
    }
    @GetMapping("/feed")
    fun getArticlesFeed(): ResponseEntity<List<Article>> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        return ResponseEntity.ok(rssFeedService.getArticlesByUserGenres(email))
    }
}