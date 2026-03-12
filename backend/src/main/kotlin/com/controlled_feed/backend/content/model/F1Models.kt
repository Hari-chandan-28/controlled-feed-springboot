package com.controlled_feed.backend.content.model

data class DriverStanding(
    val position: String = "",
    val driverName: String = "",
    val team: String = "",
    val points: String="",
    val wins:String="",
    val nationality:String="",
    val podiums: Int = 0
)
data class RaceResult(
    val position: String = "",
    val driverName: String = "",
    val team: String = "",
    val time: String = "",
    val fastestLap: String = "",
    val points: String = ""
)
data class RaceSchedule(
    val raceName: String = "",
    val circuit: String = "",
    val country: String = "",
    val date: String = "",
    val time: String = ""
)
data class LiveDriverPosition(
    val driverNumber:Int = 0,
    val driverName: String = "",
    val teamName:String = "",
    val position:Int=0,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val date: String = "",
)
data class LiveTiming(
    val driverNumber:Int = 0,
    val lapNumber:Int = 0,
    val lapDuration: Double? = null,
    val sector1: Double? = null,
    val sector2: Double?= null,
    val sector3: Double?= null,
    val isPitOutLap:Boolean = false,
    val date:String = ""
)
data class LiveInterval(
    val driverNumber:Int = 0,
    val gapToLeader:String = "",
    val interval:String = "",
    val date:String = ""
)
data class ConstructorStanding(
    val position: String = "",
    val teamName: String = "",
    val nationality: String = "",
    val points: String = "",
    val wins: String = "",
    val podiums: Int = 0
)