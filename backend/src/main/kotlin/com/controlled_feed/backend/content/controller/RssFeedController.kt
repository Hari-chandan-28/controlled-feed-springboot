package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.Article
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.service.RssFeedService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rss")
class RssFeedController(private val rssFeedService: RssFeedService) {

    // Personalized feed by user genres — frontend uses this
    @GetMapping("/feed")
    fun getArticlesFeed(): ResponseEntity<List<Article>> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        return ResponseEntity.ok(rssFeedService.getArticlesByUserGenres(email))
    }

    // Get articles by category — works for ANY sport, no code change needed
    // e.g. /api/rss/articles?category=FOOTBALL
    @GetMapping("/articles")
    fun getArticlesByCategory(
        @RequestParam category: String
    ): ResponseEntity<List<Article>> {
        val cat = try { VideoCategory.valueOf(category.uppercase()) }
        catch (e: Exception) { return ResponseEntity.badRequest().build() }
        return ResponseEntity.ok(rssFeedService.getArticlesByCategory(cat))
    }

    // Manually trigger fetch for one sport — useful for testing
    // e.g. /api/rss/fetch?category=TENNIS
    @GetMapping("/fetch")
    fun fetchBySport(@RequestParam category: String): ResponseEntity<List<Article>> {
        val cat = try { VideoCategory.valueOf(category.uppercase()) }
        catch (e: Exception) { return ResponseEntity.badRequest().build() }
        return ResponseEntity.ok(rssFeedService.fetchAndStoreSport(cat))
    }

    // Manually trigger fetch for ALL sports at once
    @GetMapping("/fetch/all")
    fun fetchAll(): ResponseEntity<Map<String, Int>> {
        val results = rssFeedService.fetchAndStoreAllArticles()
        return ResponseEntity.ok(mapOf("saved" to results.size))
    }
}