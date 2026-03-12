package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.BattingEntry
import com.controlled_feed.backend.content.model.BowlingEntry
import com.controlled_feed.backend.content.model.CricketMatch
import com.controlled_feed.backend.content.model.CricketScore
import com.controlled_feed.backend.content.model.CricketScorecard
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.apache.coyote.Response
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

// Temporary API Only for learning can be removed in the future


@Service
class CricketService {
    private val logger = LoggerFactory.getLogger(CricketService::class.java)
    @Value("\${cricket.api.key}")
    lateinit var apiKey: String
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://api.cricapi.com/v1")
        .build()

    @Cacheable(value = ["cricket-live"], key = "'live'")
    @CircuitBreaker(name = "cricketService", fallbackMethod = "fallbackLiveMatches")
     fun getLiveMatches():List<CricketMatch>{
        logger.info("🏏 Fetching live cricket matches...")
        val response = webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/currentMatches")
                    .queryParam("apikey", apiKey)
                    .queryParam("offset", 0)
                    .build()
            }
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()
        return extractMatches(response)
    }
    @Cacheable(value = ["cricket-scorecard"], key = "#matchId")
    @CircuitBreaker(name = "cricketService", fallbackMethod = "fallbackScorecard")
     fun getScoreCard(matchId:String):CricketScorecard{
        logger.info("Fetching score card $matchId")
        val response = webClient.get()
            .uri{uriBuilder ->
                uriBuilder
                    .path("/match_scorecard")
                    .queryParam("apikey",apiKey)
                    .queryParam("id",matchId)
                    .build()
            }
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()
        return extractScorecard(response,matchId)
    }
    @Cacheable(value = ["cricket-upcoming"], key = "'upcoming'")
    @CircuitBreaker(name = "cricketService", fallbackMethod = "fallbackUpcomingMatches")
     fun getUpComingMatches():List<CricketMatch>{
    logger.info("Fetching up coming matches...")
        val response = webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/matches")
                    .queryParam("apiKey",apiKey)
                    .queryParam("offset",0)
                .build()
            }
        .retrieve()
        .bodyToMono(Map::class.java)
        .block()

        return extractMatches(response)
    }
    fun fallbackLiveMatches(e: Exception): List<CricketMatch> {
        logger.error("Circuit OPEN for live matches! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackScorecard(e: Exception, matchId: String): CricketScorecard {
        logger.error("Circuit OPEN for scorecard! Error: ${e.message}")
        return CricketScorecard(matchId = matchId)
    }

    fun fallbackUpcomingMatches(e: Exception): List<CricketMatch> {
        logger.error("Circuit OPEN for upcoming matches! Error: ${e.message}")
        return emptyList()
    }
    @Suppress("UNCHECKED_CAST")
    private fun extractMatches(response: Map<*,*>?):List<CricketMatch>{
        val data = response?.get("data")as? List<*> ?:return emptyList()
        return data.mapNotNull { match ->
            val m= match as?Map<*,*>?:return@mapNotNull null
            val scores =(m["score"] as? List<*>)?.mapNotNull { score->
                val s= score as?Map<*,*>?:return@mapNotNull null
                CricketScore(
                    inning =s["inning"]?.toString()?:"",
                    runs =(s["r"]as?Int)?:0,
                    wickets = (s["w"] as? Int) ?: 0,
                    overs = (s["o"] as? Number)?.toDouble() ?: 0.0
                )
            }?: emptyList()

            CricketMatch(
                id = m["id"]?.toString() ?: "",
                name = m["name"]?.toString() ?: "",
                matchType = m["matchType"]?.toString() ?: "",
                status = m["status"]?.toString() ?: "",
                venue = m["venue"]?.toString() ?: "",
                teams = (m["teams"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                scores = scores,
                dateTime = m["dateTimeGMT"]?.toString() ?: ""
            )
        }
    }
    @Suppress("UNCHECKED_CAST")
    private fun extractScorecard(response: Map<*,*>?, matchId: String): CricketScorecard {
        val data = response?.get("data") as? Map<*, *> ?: return CricketScorecard(matchId = matchId)
        val scorecard = data["scorecard"] as? List<*> ?: emptyList<Any>()

        val battingList = mutableListOf<BattingEntry>()
        val bowlingList = mutableListOf<BowlingEntry>()

        scorecard.forEach { innings ->
            val inn = innings as? Map<*, *> ?: return@forEach

            // Batting
            val batting = inn["batting"] as? List<*> ?: emptyList<Any>()
            batting.forEach { batsman ->
                val b = batsman as? Map<*, *> ?: return@forEach
                battingList.add(
                    BattingEntry(
                        player = b["batsman"]?.toString() ?: "",
                        runs = (b["r"] as? Int) ?: 0,
                        balls = (b["b"] as? Int) ?: 0,
                        fours = (b["4s"] as? Int) ?: 0,
                        sixes = (b["6s"] as? Int) ?: 0,
                        strikeRate = (b["sr"] as? Number)?.toDouble() ?: 0.0,
                        dismissal = b["dismissal"]?.toString() ?: "not out"
                    )
                )
            }

            // Bowling
            val bowling = inn["bowling"] as? List<*> ?: emptyList<Any>()
            bowling.forEach { bowler ->
                val b = bowler as? Map<*, *> ?: return@forEach
                bowlingList.add(
                    BowlingEntry(
                        player = b["bowler"]?.toString() ?: "",
                        overs = (b["o"] as? Number)?.toDouble() ?: 0.0,
                        maidens = (b["m"] as? Int) ?: 0,
                        runs = (b["r"] as? Int) ?: 0,
                        wickets = (b["w"] as? Int) ?: 0,
                        economy = (b["eco"] as? Number)?.toDouble() ?: 0.0
                    )
                )
            }
        }
        return CricketScorecard(
            matchId = matchId,
            name = data["name"]?.toString() ?: "",
            status = data["status"]?.toString() ?: "",
            venue = data["venue"]?.toString() ?: "",
            batting = battingList,
            bowling = bowlingList
        )
    }
}