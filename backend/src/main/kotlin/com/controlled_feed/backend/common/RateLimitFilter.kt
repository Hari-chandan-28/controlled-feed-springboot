package com.controlled_feed.backend.common

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.cglib.core.CollectionUtils.bucket
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


@Component
class RateLimitFilter : OncePerRequestFilter() {
    private val buckets= ConcurrentHashMap<String, Bucket>()

    private fun getBucket(ip: String,limit: Long,duration: Duration): Bucket {
        return buckets.getOrPut(ip){
            val bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit,duration)
                .build()
            Bucket.builder()
                .addLimit(bandwidth)
                .build()
        }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val ip = request.remoteAddr
        val path = request.requestURI
        val bucket = when{
            path.contains("/api/auth/signup")-> getBucket("$ip-signup",5, Duration.ofMinutes(1))
            path.contains("/api/auth/login") -> getBucket("$ip-login", 10, Duration.ofMinutes(1))
            path.contains("/api/youtube") -> getBucket("$ip-youtube", 10, Duration.ofMinutes(1))
            path.contains("/api/feed") -> getBucket("$ip-feed", 60, Duration.ofMinutes(1))
            else -> getBucket("$ip-general",30, Duration.ofMinutes(1))
        }
        if(bucket.tryConsume(1)){
            filterChain.doFilter(request, response)
        }
        else{
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("""
                {
                "status": 429,
                "error": "Too many requests.",
                "message": "Rate limit exceeded, please try again later."
                }""".trimIndent())
        }
    }
}