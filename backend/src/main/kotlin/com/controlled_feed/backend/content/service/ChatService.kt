package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.ChatResponse
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.HttpProtocol
import reactor.netty.http.client.HttpClient

@Service
class ChatService {

    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    @Value("\${gemini.api.key}")
    lateinit var apiKey: String

    private val httpClient = HttpClient.create()
        .protocol(HttpProtocol.HTTP11)

    private val webClient: WebClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .build()

    private val systemPrompt = """
        You are a sports expert assistant specializing in F1 and Cricket only.
        You can answer questions about:
        - Formula 1 racing, drivers, teams, races, circuits, history
        - Cricket matches, players, teams, tournaments, rules, records
        - IPL, Test cricket, ODI, T20 matches
        - F1 championship standings, race results, lap times
        Rules:
        - Only answer F1 and Cricket related questions
        - If asked anything else say: "I can only answer questions about F1 and Cricket!"
        - Keep answers clear and concise
    """.trimIndent()

    fun askQuestion(question: String): ChatResponse {
        logger.info("Processing question: $question")
        return try {
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to "$systemPrompt\n\nUser Question: $question")
                        )
                    )
                )
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val response = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val answer = extractAnswer(response)
            logger.info("Answer generated successfully")
            ChatResponse(answer = answer, question = question)

        } catch (e: Exception) {
            logger.error("Error Calling Gemini API: ${e.message}")
            ChatResponse(
                answer = "Sorry I am unable to answer right now. Please try again later!",
                question = question
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractAnswer(response: Map<*, *>?): String {
        return try {
            val candidates = response?.get("candidates") as? List<*> ?: return "No answer found"
            val content = (candidates.firstOrNull() as? Map<*, *>)?.get("content") as? Map<*, *>
                ?: return "No answer found"
            val parts = content["parts"] as? List<*> ?: return "No answer found"
            (parts.firstOrNull() as? Map<*, *>)?.get("text")?.toString() ?: "No answer found"
        } catch (e: Exception) {
            "Sorry I could not process the answer!"
        }
    }
}