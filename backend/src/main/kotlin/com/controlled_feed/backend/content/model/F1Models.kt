package com.controlled_feed.backend.content.model
import java.io.Serializable
data class DriverStanding(
    val position: String = "",
    val driverName: String = "",
    val team: String = "",
    val points: String="",
    val wins:String="",
    val nationality:String="",
    val podiums: Int = 0
): Serializable
data class RaceResult(
    val position: String = "",
    val driverName: String = "",
    val team: String = "",
    val time: String = "",
    val fastestLap: String = "",
    val points: String = ""
): Serializable
data class RaceSchedule(
    val raceName: String = "",
    val circuit: String = "",
    val country: String = "",
    val date: String = "",
    val time: String = ""
): Serializable
data class LiveDriverPosition(
    val driverNumber:Int = 0,
    val driverName: String = "",
    val teamName:String = "",
    val position:Int=0,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val date: String = "",
): Serializable
data class LiveTiming(
    val driverNumber:Int = 0,
    val lapNumber:Int = 0,
    val lapDuration: Double? = null,
    val sector1: Double? = null,
    val sector2: Double?= null,
    val sector3: Double?= null,
    val isPitOutLap:Boolean = false,
    val date:String = ""
): Serializable
data class LiveInterval(
    val driverNumber:Int = 0,
    val gapToLeader:String = "",
    val interval:String = "",
    val date:String = ""
): Serializable
data class ConstructorStanding(
    val position: String = "",
    val teamName: String = "",
    val nationality: String = "",
    val points: String = "",
    val wins: String = "",
    val podiums: Int = 0
): Serializable

//new Model for Scheduling

data class RaceDetail(
    val raceName: String = "",
    val round: String = "",
    val date: String = "",
    val circuit: String = "",
    val country: String = "",
    // Completed race data
    val podium: List<PodiumEntry> = emptyList(),
    val fastestLap: FastestLapEntry? = null,
    val fastestPitStop: PitStopEntry? = null,
    // Upcoming race session times (UTC)
    val sessions: List<SessionTime> = emptyList(),
    val hasSprint: Boolean = false
): Serializable
data class PodiumEntry(
    val position: Int = 0,
    val driverName: String = "",
    val team: String = "",
    val time: String = "",
    val points: String = ""
): Serializable
data class FastestLapEntry(
    val driverName: String = "",
    val team: String = "",
    val lapTime: String = "",
    val lapNumber: String = ""
): Serializable
data class PitStopEntry(
    val driverName: String = "",
    val lap: String = "",
    val duration: String = "",
    val stop: String = ""
): Serializable
data class SessionTime(
    val name: String = "",    // "FP1", "FP2", "FP3", "Qualifying", "Sprint", "Race"
    val date: String = "",    // UTC date
    val time: String = ""     // UTC time e.g. "05:00:00Z"
): Serializable