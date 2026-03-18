package com.controlled_feed.backend.content.model

data class ChatRequest(
    val question: String = ""
)
data class ChatResponse(
    val answer: String = "",
    val question: String = ""
)