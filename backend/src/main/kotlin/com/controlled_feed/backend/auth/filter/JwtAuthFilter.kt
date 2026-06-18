package com.controlled_feed.backend.auth.filter

import com.controlled_feed.backend.auth.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authHeader = request.getHeader("Authorization")
        val queryToken = request.getParameter("token")

        // Resolve token from EITHER source before deciding whether to bail out.
        // This must happen first — bailing out based on authHeader alone
        // would skip the query-param path entirely for SSE requests.
        val token = when {
            authHeader?.startsWith("Bearer ") == true -> authHeader.substring(7)
            request.requestURI.contains("/live/stream") && queryToken != null -> queryToken
            else -> null
        }

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        if (jwtService.isTokenValid(token)) {
            val email = jwtService.extractEmail(token)
            val authentication = UsernamePasswordAuthenticationToken(email, null, emptyList())
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}