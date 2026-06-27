package com.controlled_feed.backend.common

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    private fun getBucket(key: String, limit: Long, duration: Duration): Bucket {
        return buckets.getOrPut(key) {
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, duration)
                        .build()
                )
                .build()
        }
    }

    // Paths that must NEVER be rate limited
    private fun shouldSkip(path: String): Boolean {
        return path.contains("/api/f1/live/stream") ||  // SSE — long-lived connection
                path.contains("/actuator")               // health checks
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        // SSE and health endpoints bypass rate limiting entirely
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response)
            return
        }

        val ip = request.remoteAddr

        val bucket = when {
            // ── Auth — strict, brute force protection ──────────
            path.contains("/api/auth/signup") ->
                getBucket("$ip:signup", 10, Duration.ofMinutes(1))

            path.contains("/api/auth/login") ->
                getBucket("$ip:login", 20, Duration.ofMinutes(1))

            // ── AI Chat — Gemini has its own quota ─────────────
            path.contains("/api/chat") ->
                getBucket("$ip:chat", 30, Duration.ofMinutes(1))

            // ── YouTube manual trigger — rarely called ──────────
            path.contains("/api/youtube") ->
                getBucket("$ip:youtube", 20, Duration.ofMinutes(1))

            // ── F1 live endpoints (non-SSE) — polling endpoints ─
            // positions/timing/intervals are polled every few seconds
            path.contains("/api/f1/live") ->
                getBucket("$ip:f1-live", 300, Duration.ofMinutes(1))

            // ── F1 static data — dashboard tabs ────────────────
            // 4 tabs × multiple users × Redis cached = still low actual hits
            path.contains("/api/f1") ->
                getBucket("$ip:f1", 200, Duration.ofMinutes(1))

            // ── Cricket — live + upcoming + scorecard clicks ────
            path.contains("/api/cricket") ->
                getBucket("$ip:cricket", 200, Duration.ofMinutes(1))

            // ── Feed — main content page, tab switching ─────────
            // Discover + 5 sport tabs × pagination = high call count
            path.contains("/api/feed") ->
                getBucket("$ip:feed", 400, Duration.ofMinutes(1))

            // ── RSS articles — loaded once, cached client side ──
            path.contains("/api/rss") ->
                getBucket("$ip:rss", 200, Duration.ofMinutes(1))

            // ── Profile — loaded on feed + profile + edit modal ─
            path.contains("/api/profile") ->
                getBucket("$ip:profile", 100, Duration.ofMinutes(1))

            // ── User update endpoints ────────────────────────────
            path.contains("/api/user") ->
                getBucket("$ip:user", 30, Duration.ofMinutes(1))

            // ── Everything else ──────────────────────────────────
            else ->
                getBucket("$ip:general", 100, Duration.ofMinutes(1))
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write(
                """{"status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Please try again later."}"""
            )
        }
    }
}