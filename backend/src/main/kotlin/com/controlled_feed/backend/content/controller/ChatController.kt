package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.ChatRequest
import com.controlled_feed.backend.content.model.ChatResponse
import com.controlled_feed.backend.content.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {
    @PostMapping("/ask")
    fun askQuestion(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse>{
        return ResponseEntity.ok(chatService.askQuestion(request.question))
    }
}