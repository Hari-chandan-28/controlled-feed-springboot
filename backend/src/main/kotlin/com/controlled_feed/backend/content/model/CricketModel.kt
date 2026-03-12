package com.controlled_feed.backend.content.model
import java.io.Serializable

// Temporary API Only for learning can be removed in the future

data class CricketMatch(
    val id: String ="",
    val name: String="",
    val matchType: String="",
    val status: String="",
    val venue: String="",
    val teams:List<String> = emptyList(),
    val scores:List<CricketScore> = emptyList(),
    val dateTime:String="",
): Serializable
data class CricketScore(
    val inning:String="",
    val runs:Int=0,
    val wickets:Int=0,
    val overs:Double=0.0
): Serializable
data class CricketScorecard(
    val matchId:String="",
    val name:String="",
    val status:String="",
    val venue:String="",
    val batting:List<BattingEntry> = emptyList(),
    val bowling:List<BowlingEntry> = emptyList(),
): Serializable
data class BattingEntry(
    val player: String = "",
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val strikeRate: Double = 0.0,
    val dismissal: String = ""
) : Serializable

data class BowlingEntry(
    val player: String = "",
    val overs: Double = 0.0,
    val maidens: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val economy: Double = 0.0
) : Serializable
