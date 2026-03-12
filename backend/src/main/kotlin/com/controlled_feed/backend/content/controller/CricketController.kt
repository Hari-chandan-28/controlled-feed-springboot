package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.service.CricketService
import com.controlled_feed.backend.content.model.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.*

// Temporary API Only for learning can be removed in the future

@RestController
@RequestMapping("/api/cricket")
class CricketController(private val cricketService: CricketService) {
    @GetMapping("/live")
    fun getLiveMatches(): ResponseEntity<List<CricketMatch>>{
        return ResponseEntity.ok(cricketService.getLiveMatches())
    }
    @GetMapping("/scorecard/{matchId}")
    fun getScorecard(@PathVariable matchId: String): ResponseEntity<CricketScorecard>{
        return ResponseEntity.ok(cricketService.getScoreCard(matchId))
    }
    @GetMapping("/upcoming")
    fun getUpcomingMatches(): ResponseEntity<List<CricketMatch>>{
        return ResponseEntity.ok(cricketService.getUpComingMatches())
    }
}